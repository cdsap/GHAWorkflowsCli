package io.github.cdsap.ghacli

import io.github.cdsap.ghacli.models.Job
import io.github.cdsap.ghacli.models.JobStep
import io.github.cdsap.ghacli.models.WorkflowRun
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class MetricsCollectorTest {
    
    private val collector = MetricsCollector()
    
    @Test
    fun `collectMetrics should collect workflow durations`() = runBlocking {
        val runs = listOf(
            createRun("2024-01-01T10:00:00Z", "2024-01-01T10:05:00Z")
        )
        
        val metrics = collector.collectMetrics(runs) { emptyList() }
        
        assertEquals(1, metrics.workflowDurations.size)
        assertEquals(300, metrics.workflowDurations.first()) // 5 minutes
    }
    
    @Test
    fun `collectMetrics should collect job durations`() = runBlocking {
        val runs = listOf(
            createRun("2024-01-01T10:00:00Z", "2024-01-01T10:05:00Z")
        )
        val jobs = listOf(
            createJob("Job1", "2024-01-01T10:00:00Z", "2024-01-01T10:03:00Z")
        )
        
        val metrics = collector.collectMetrics(runs) { jobs }
        
        assertEquals(1, metrics.jobDurations.size)
        assertEquals(180, metrics.jobDurations.first()) // 3 minutes
        assertEquals(1, metrics.jobMetricsMap.size)
        assertTrue(metrics.jobMetricsMap.containsKey("Job1"))
    }
    
    @Test
    fun `collectMetrics should collect step durations and success info`() = runBlocking {
        val runs = listOf(
            createRun("2024-01-01T10:00:00Z", "2024-01-01T10:05:00Z")
        )
        val jobs = listOf(
            createJobWithSteps(
                "Job1",
                listOf(
                    createStep("Step1", "2024-01-01T10:00:00Z", "2024-01-01T10:01:00Z", "success"),
                    createStep("Step2", "2024-01-01T10:01:00Z", "2024-01-01T10:02:00Z", "failure")
                )
            )
        )
        
        val metrics = collector.collectMetrics(runs) { jobs }
        
        assertEquals(2, metrics.stepDurations.size)
        assertEquals(2, metrics.stepMetricsMap.size)
        assertTrue(metrics.stepMetricsMap.containsKey("Job1:Step1"))
        assertTrue(metrics.stepMetricsMap.containsKey("Job1:Step2"))
        
        val step1Success = metrics.stepSuccessMap["Job1:Step1"]
        assertNotNull(step1Success)
        assertEquals(1, step1Success!!.first) // success count
        assertEquals(1, step1Success.second) // total count
        
        val step2Success = metrics.stepSuccessMap["Job1:Step2"]
        assertNotNull(step2Success)
        assertEquals(0, step2Success!!.first) // success count
        assertEquals(1, step2Success.second) // total count
    }
    
    @Test
    fun `collectMetrics should aggregate step durations per job`() = runBlocking {
        val runs = listOf(
            createRun("2024-01-01T10:00:00Z", "2024-01-01T10:05:00Z")
        )
        val jobs = listOf(
            createJobWithSteps(
                "Job1",
                listOf(
                    createStep("Step1", "2024-01-01T10:00:00Z", "2024-01-01T10:01:00Z", "success"),
                    createStep("Step2", "2024-01-01T10:01:00Z", "2024-01-01T10:02:00Z", "success")
                )
            )
        )
        
        val metrics = collector.collectMetrics(runs) { jobs }
        
        assertTrue(metrics.jobStepMetricsMap.containsKey("Job1"))
        assertEquals(2, metrics.jobStepMetricsMap["Job1"]!!.size)
    }
    
    @Test
    fun `collectMetrics should track rerun statistics`() = runBlocking {
        val runs = listOf(
            createRun("2024-01-01T10:00:00Z", "2024-01-01T10:05:00Z", runAttempt = 1),
            createRun("2024-01-01T10:00:00Z", "2024-01-01T10:05:00Z", runAttempt = 2),
            createRun("2024-01-01T10:00:00Z", "2024-01-01T10:05:00Z", runAttempt = 1)
        )
        
        val metrics = collector.collectMetrics(runs) { emptyList() }
        
        assertEquals(2, metrics.originalRunCount)
        assertEquals(1, metrics.rerunCount)
    }
    
    private fun createRun(createdAt: String, updatedAt: String, runAttempt: Int = 1): WorkflowRun {
        return WorkflowRun(
            id = 1,
            name = "Test",
            status = "completed",
            conclusion = "success",
            created_at = createdAt,
            updated_at = updatedAt,
            run_started_at = createdAt,
            head_branch = "main",
            workflow_id = 1,
            html_url = "https://github.com",
            run_number = 1,
            jobs_url = "https://github.com",
            run_attempt = runAttempt
        )
    }
    
    private fun createJob(name: String, startedAt: String, completedAt: String): Job {
        return Job(
            id = 1,
            name = name,
            status = "completed",
            conclusion = "success",
            started_at = startedAt,
            completed_at = completedAt,
            steps = null
        )
    }
    
    private fun createJobWithSteps(name: String, steps: List<JobStep>): Job {
        return Job(
            id = 1,
            name = name,
            status = "completed",
            conclusion = "success",
            started_at = "2024-01-01T10:00:00Z",
            completed_at = "2024-01-01T10:05:00Z",
            steps = steps
        )
    }
    
    private fun createStep(name: String, startedAt: String, completedAt: String, conclusion: String): JobStep {
        return JobStep(
            name = name,
            status = "completed",
            conclusion = conclusion,
            number = 1,
            started_at = startedAt,
            completed_at = completedAt
        )
    }
}

