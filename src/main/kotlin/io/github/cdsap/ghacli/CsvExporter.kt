package io.github.cdsap.ghacli

import java.io.File
import java.io.FileWriter

data class WorkflowMetrics(
    val workflowDurations: List<Long>,
    val jobDurations: List<Long>,
    val stepDurations: List<Long>
)

data class StepMetricsData(
    val durations: List<Long>,
    val successCount: Int,
    val totalCount: Int
)

data class JobMetrics(
    val jobName: String,
    val jobDurations: List<Long>,
    val stepDurations: List<Long>,
    val stepMetrics: Map<String, StepMetricsData> // Map of step name to metrics data
)

object CsvExporter {
    /**
     * Escapes a CSV field value according to RFC 4180:
     * - If the value contains comma, newline, or double quote, wrap it in double quotes
     * - Escape any double quotes within the value by doubling them
     */
    private fun escapeCsvField(value: String): String {
        return if (value.contains(',') || value.contains('\n') || value.contains('"')) {
            "\"${value.replace("\"", "\"\"")}\""
        } else {
            value
        }
    }
    
    fun exportMetrics(metrics: WorkflowMetrics, outputFile: String = "workflow_summary.csv", totalWorkflowRuns: Int = 0) {
        val file = File(outputFile)
        FileWriter(file).use { writer ->
            // Write header
            writer.appendLine("Metric Type,Mean,Median,P90,Count")
            
            // Write workflow statistics only
            val workflowMean = StatisticsCalculator.calculateMean(metrics.workflowDurations)
            val workflowMedian = StatisticsCalculator.calculateMedian(metrics.workflowDurations)
            val workflowP90 = StatisticsCalculator.calculateP90(metrics.workflowDurations)
            writer.appendLine("Workflow Duration,${workflowMean},${workflowMedian},${workflowP90},${metrics.workflowDurations.size}")
        }
    }
    
    fun exportJobMetrics(jobMetrics: JobMetrics, outputDir: String = ".", totalWorkflowRuns: Int = 0): String {
        val sanitizedJobName = jobMetrics.jobName
            .replace(Regex("[^a-zA-Z0-9_-]"), "_")
            .replace(Regex("_{2,}"), "_")
        
        // Limit filename length to avoid filesystem limits (max 255 chars, leave room for path and extension)
        // Use hash suffix if truncated to ensure uniqueness
        val maxJobNameLength = 150
        val fileNameBase = if (sanitizedJobName.length > maxJobNameLength) {
            val truncated = sanitizedJobName.substring(0, maxJobNameLength)
            // Use absolute value of hash code to avoid negative numbers in filename
            val hash = kotlin.math.abs(sanitizedJobName.hashCode()).toString()
            "${truncated}_${hash}"
        } else {
            sanitizedJobName
        }
        
        val fileName = "${outputDir}/gha_metrics_job_${fileNameBase}.csv"
        val file = File(fileName)
        
        FileWriter(file).use { writer ->
            // Write header
            writer.appendLine("Metric Type,Name,Mean,Median,P90,Count")
            
            // Write job duration statistics
            if (jobMetrics.jobDurations.isNotEmpty()) {
                val jobMean = StatisticsCalculator.calculateMean(jobMetrics.jobDurations)
                val jobMedian = StatisticsCalculator.calculateMedian(jobMetrics.jobDurations)
                val jobP90 = StatisticsCalculator.calculateP90(jobMetrics.jobDurations)
                writer.appendLine("Job Duration,${escapeCsvField(jobMetrics.jobName)},${jobMean},${jobMedian},${jobP90},${jobMetrics.jobDurations.size}")
            }
            
            // Write aggregated step duration statistics
            if (jobMetrics.stepDurations.isNotEmpty()) {
                val stepMean = StatisticsCalculator.calculateMean(jobMetrics.stepDurations)
                val stepMedian = StatisticsCalculator.calculateMedian(jobMetrics.stepDurations)
                val stepP90 = StatisticsCalculator.calculateP90(jobMetrics.stepDurations)
                writer.appendLine("Step Duration (All Steps),${escapeCsvField(jobMetrics.jobName)},${stepMean},${stepMedian},${stepP90},${jobMetrics.stepDurations.size}")
            }
            
            // Write individual step statistics
            jobMetrics.stepMetrics.forEach { (stepName, stepData) ->
                if (stepData.durations.isNotEmpty()) {
                    val stepMean = StatisticsCalculator.calculateMean(stepData.durations)
                    val stepMedian = StatisticsCalculator.calculateMedian(stepData.durations)
                    val stepP90 = StatisticsCalculator.calculateP90(stepData.durations)
                    writer.appendLine("Step Duration,${escapeCsvField(stepName)},${stepMean},${stepMedian},${stepP90},${stepData.durations.size}")
                }
            }
        }
        
        return fileName
    }
    
    fun exportJobsSummary(
        allJobMetrics: Map<String, JobMetrics>,
        totalWorkflowRuns: Int,
        outputFile: String = "gha_jobs_summary.csv"
    ) {
        val file = File(outputFile)
        FileWriter(file).use { writer ->
            writer.appendLine("Job Name,Mean Duration,Median Duration,P90 Duration,Count")
            
            allJobMetrics.forEach { (jobName, jobMetrics) ->
                if (jobMetrics.jobDurations.isNotEmpty()) {
                    val jobMean = StatisticsCalculator.calculateMean(jobMetrics.jobDurations)
                    val jobMedian = StatisticsCalculator.calculateMedian(jobMetrics.jobDurations)
                    val jobP90 = StatisticsCalculator.calculateP90(jobMetrics.jobDurations)
                    writer.appendLine("${escapeCsvField(jobName)},${jobMean},${jobMedian},${jobP90},${jobMetrics.jobDurations.size}")
                }
            }
        }
    }
    
    fun exportStepsSummary(
        allJobMetrics: Map<String, JobMetrics>,
        totalWorkflowRuns: Int,
        outputFile: String = "gha_steps_summary.csv"
    ) {
        val file = File(outputFile)
        FileWriter(file).use { writer ->
            writer.appendLine("Job Name,Step Name,Mean Duration,Median Duration,P90 Duration,Count")
            
            allJobMetrics.forEach { (jobName, jobMetrics) ->
                jobMetrics.stepMetrics.forEach { (stepName, stepData) ->
                    if (stepData.durations.isNotEmpty()) {
                        val stepMean = StatisticsCalculator.calculateMean(stepData.durations)
                        val stepMedian = StatisticsCalculator.calculateMedian(stepData.durations)
                        val stepP90 = StatisticsCalculator.calculateP90(stepData.durations)
                        writer.appendLine("${escapeCsvField(jobName)},${escapeCsvField(stepName)},${stepMean},${stepMedian},${stepP90},${stepData.durations.size}")
                    }
                }
            }
        }
    }
    
    fun exportDetailedMetrics(
        workflowDurations: List<Long>,
        jobDurations: List<Long>,
        stepDurations: List<Long>,
        outputFile: String = "gha_detailed_metrics.csv"
    ) {
        val file = File(outputFile)
        FileWriter(file).use { writer ->
            // Write header
            writer.appendLine("Type,Name,Duration (seconds),Duration (formatted)")
            
            // Write workflow durations
            workflowDurations.forEachIndexed { index, duration ->
                writer.appendLine("Workflow,Run ${index + 1},$duration,${StatisticsCalculator.formatSeconds(duration.toDouble())}")
            }
            
            // Write job durations
            jobDurations.forEachIndexed { index, duration ->
                writer.appendLine("Job,Job ${index + 1},$duration,${StatisticsCalculator.formatSeconds(duration.toDouble())}")
            }
            
            // Write step durations
            stepDurations.forEachIndexed { index, duration ->
                writer.appendLine("Step,Step ${index + 1},$duration,${StatisticsCalculator.formatSeconds(duration.toDouble())}")
            }
        }
    }
}

