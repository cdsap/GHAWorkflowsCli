package io.github.cdsap.ghacli

import io.github.cdsap.ghacli.models.Job
import io.github.cdsap.ghacli.models.JobStep
import io.github.cdsap.ghacli.models.WorkflowRun

data class CollectedMetrics(
    val workflowDurations: List<Long>,
    val jobDurations: List<Long>,
    val stepDurations: List<Long>,
    val jobMetricsMap: Map<String, List<Long>>,
    val jobStepMetricsMap: Map<String, List<Long>>,
    val stepMetricsMap: Map<String, List<Long>>,
    val stepSuccessMap: Map<String, Pair<Int, Int>>, // Pair<successCount, totalCount>
    val rerunCount: Int = 0,
    val originalRunCount: Int = 0
)

class MetricsCollector {
    suspend fun collectMetrics(
        runs: List<WorkflowRun>,
        jobsProvider: suspend (WorkflowRun) -> List<Job>
    ): CollectedMetrics {
        val workflowDurations = mutableListOf<Long>()
        val jobDurations = mutableListOf<Long>()
        val stepDurations = mutableListOf<Long>()
        
        val jobMetricsMap = mutableMapOf<String, MutableList<Long>>()
        val jobStepMetricsMap = mutableMapOf<String, MutableList<Long>>()
        val stepMetricsMap = mutableMapOf<String, MutableList<Long>>()
        val stepSuccessMap = mutableMapOf<String, Pair<Int, Int>>()
        var rerunCount = 0
        var originalRunCount = 0
        
        runs.forEach { run ->
            // Track rerun statistics
            if (run.run_attempt > 1) {
                rerunCount++
            } else {
                originalRunCount++
            }
            val runDuration = DurationCalculator.calculateRunDuration(run)
            if (runDuration != null) {
                workflowDurations.add(runDuration)
            }
            
            val jobs = jobsProvider(run)
            jobs.forEach { job ->
                val jobDuration = DurationCalculator.calculateJobDuration(job)
                if (jobDuration != null) {
                    jobDurations.add(jobDuration)
                    jobMetricsMap.getOrPut(job.name) { mutableListOf() }.add(jobDuration)
                }
                
                job.steps?.forEach { step ->
                    collectStepMetrics(job, step, stepDurations, jobStepMetricsMap, stepMetricsMap, stepSuccessMap)
                }
            }
        }
        
        return CollectedMetrics(
            workflowDurations = workflowDurations,
            jobDurations = jobDurations,
            stepDurations = stepDurations,
            jobMetricsMap = jobMetricsMap,
            jobStepMetricsMap = jobStepMetricsMap,
            stepMetricsMap = stepMetricsMap,
            stepSuccessMap = stepSuccessMap,
            rerunCount = rerunCount,
            originalRunCount = originalRunCount
        )
    }
    
    private fun collectStepMetrics(
        job: Job,
        step: JobStep,
        stepDurations: MutableList<Long>,
        jobStepMetricsMap: MutableMap<String, MutableList<Long>>,
        stepMetricsMap: MutableMap<String, MutableList<Long>>,
        stepSuccessMap: MutableMap<String, Pair<Int, Int>>
    ) {
        val stepKey = "${job.name}:${step.name}"
        
        // Track step success/failure
        val isSuccess = step.conclusion == "success"
        val currentSuccess = stepSuccessMap.getOrPut(stepKey) { Pair(0, 0) }
        stepSuccessMap[stepKey] = Pair(
            currentSuccess.first + if (isSuccess) 1 else 0,
            currentSuccess.second + 1
        )
        
        val stepDuration = DurationCalculator.calculateStepDuration(step)
        if (stepDuration != null) {
            stepDurations.add(stepDuration)
            jobStepMetricsMap.getOrPut(job.name) { mutableListOf() }.add(stepDuration)
            stepMetricsMap.getOrPut(stepKey) { mutableListOf() }.add(stepDuration)
        }
    }
}

