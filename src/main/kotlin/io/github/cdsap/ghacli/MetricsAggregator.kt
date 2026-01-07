package io.github.cdsap.ghacli

import java.io.File

class MetricsAggregator {
    fun aggregateJobMetrics(
        collectedMetrics: CollectedMetrics
    ): Map<String, JobMetrics> {
        val allJobMetricsMap = mutableMapOf<String, JobMetrics>()
        
        collectedMetrics.jobMetricsMap.keys.forEach { jobName ->
            val jobDurationsForJob = collectedMetrics.jobMetricsMap[jobName] ?: emptyList()
            val stepDurationsForJob = collectedMetrics.jobStepMetricsMap[jobName] ?: emptyList()
            
            // Collect all steps for this job
            val jobStepMetrics = mutableMapOf<String, StepMetricsData>()
            collectedMetrics.stepMetricsMap.keys.forEach { stepKey ->
                val parts = stepKey.split(":", limit = 2)
                if (parts.size == 2 && parts[0] == jobName) {
                    val stepName = parts[1]
                    val stepDurations = collectedMetrics.stepMetricsMap[stepKey] ?: emptyList()
                    val successInfo = collectedMetrics.stepSuccessMap[stepKey] ?: Pair(0, 0)
                    jobStepMetrics[stepName] = StepMetricsData(
                        durations = stepDurations,
                        successCount = successInfo.first,
                        totalCount = successInfo.second
                    )
                }
            }
            
            allJobMetricsMap[jobName] = JobMetrics(
                jobName = jobName,
                jobDurations = jobDurationsForJob,
                stepDurations = stepDurationsForJob,
                stepMetrics = jobStepMetrics
            )
        }
        
        return allJobMetricsMap
    }
    
    fun exportAllMetrics(
        collectedMetrics: CollectedMetrics,
        allJobMetrics: Map<String, JobMetrics>,
        totalRuns: Int,
        outputDir: String
    ): ExportResult {
        val workflowMetrics = WorkflowMetrics(
            workflowDurations = collectedMetrics.workflowDurations,
            jobDurations = collectedMetrics.jobDurations,
            stepDurations = collectedMetrics.stepDurations
        )
        
        val overallCsv = "${outputDir}/workflow_summary.csv"
        CsvExporter.exportMetrics(workflowMetrics, overallCsv, totalRuns)
        
        val exportedJobFiles = mutableListOf<String>()
        allJobMetrics.forEach { (_, jobMetrics) ->
            val fileName = CsvExporter.exportJobMetrics(jobMetrics, outputDir, totalRuns)
            exportedJobFiles.add(fileName)
        }
        
        val jobsSummary = "${outputDir}/gha_jobs_summary.csv"
        CsvExporter.exportJobsSummary(allJobMetrics, totalRuns, jobsSummary)
        
        val stepsSummary = "${outputDir}/gha_steps_summary.csv"
        CsvExporter.exportStepsSummary(allJobMetrics, totalRuns, stepsSummary)
        
        return ExportResult(
            overallCsv = overallCsv,
            jobFiles = exportedJobFiles,
            jobsSummary = jobsSummary,
            stepsSummary = stepsSummary
        )
    }
}

data class ExportResult(
    val overallCsv: String,
    val jobFiles: List<String>,
    val jobsSummary: String,
    val stepsSummary: String
)

