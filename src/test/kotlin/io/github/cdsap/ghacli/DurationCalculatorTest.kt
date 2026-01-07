package io.github.cdsap.ghacli

import io.github.cdsap.ghacli.models.Job
import io.github.cdsap.ghacli.models.JobStep
import io.github.cdsap.ghacli.models.WorkflowRun
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class DurationCalculatorTest {
    
    @Test
    fun `calculateRunDuration should return correct duration in seconds`() {
        val run = WorkflowRun(
            id = 1,
            name = "Test Workflow",
            status = "completed",
            conclusion = "success",
            created_at = "2024-01-01T10:00:00Z",
            updated_at = "2024-01-01T10:05:00Z",
            run_started_at = "2024-01-01T10:00:00Z",
            head_branch = "main",
            workflow_id = 1,
            html_url = "https://github.com",
            run_number = 1,
            jobs_url = "https://github.com"
        )
        
        val duration = DurationCalculator.calculateRunDuration(run)
        
        assertEquals(300, duration) // 5 minutes = 300 seconds
    }
    
    @Test
    fun `calculateRunDuration should use created_at if run_started_at is null`() {
        val run = WorkflowRun(
            id = 1,
            name = "Test Workflow",
            status = "completed",
            conclusion = "success",
            created_at = "2024-01-01T10:00:00Z",
            updated_at = "2024-01-01T10:02:00Z",
            run_started_at = null,
            head_branch = "main",
            workflow_id = 1,
            html_url = "https://github.com",
            run_number = 1,
            jobs_url = "https://github.com"
        )
        
        val duration = DurationCalculator.calculateRunDuration(run)
        
        assertEquals(120, duration) // 2 minutes = 120 seconds
    }
    
    @Test
    fun `calculateRunDuration should return null for invalid date format`() {
        val run = WorkflowRun(
            id = 1,
            name = "Test Workflow",
            status = "completed",
            conclusion = "success",
            created_at = "invalid-date",
            updated_at = "2024-01-01T10:02:00Z",
            run_started_at = null,
            head_branch = "main",
            workflow_id = 1,
            html_url = "https://github.com",
            run_number = 1,
            jobs_url = "https://github.com"
        )
        
        val duration = DurationCalculator.calculateRunDuration(run)
        
        assertNull(duration)
    }
    
    @Test
    fun `calculateJobDuration should return correct duration`() {
        val job = Job(
            id = 1,
            name = "Test Job",
            status = "completed",
            conclusion = "success",
            started_at = "2024-01-01T10:00:00Z",
            completed_at = "2024-01-01T10:03:00Z",
            steps = null
        )
        
        val duration = DurationCalculator.calculateJobDuration(job)
        
        assertEquals(180, duration) // 3 minutes = 180 seconds
    }
    
    @Test
    fun `calculateJobDuration should return null if started_at is missing`() {
        val job = Job(
            id = 1,
            name = "Test Job",
            status = "completed",
            conclusion = "success",
            started_at = null,
            completed_at = "2024-01-01T10:03:00Z",
            steps = null
        )
        
        val duration = DurationCalculator.calculateJobDuration(job)
        
        assertNull(duration)
    }
    
    @Test
    fun `calculateStepDuration should return correct duration`() {
        val step = JobStep(
            name = "Test Step",
            status = "completed",
            conclusion = "success",
            number = 1,
            started_at = "2024-01-01T10:00:00Z",
            completed_at = "2024-01-01T10:01:00Z"
        )
        
        val duration = DurationCalculator.calculateStepDuration(step)
        
        assertEquals(60, duration) // 1 minute = 60 seconds
    }
    
    @Test
    fun `calculateStepDuration should return null if started_at is missing`() {
        val step = JobStep(
            name = "Test Step",
            status = "completed",
            conclusion = "success",
            number = 1,
            started_at = null,
            completed_at = "2024-01-01T10:01:00Z"
        )
        
        val duration = DurationCalculator.calculateStepDuration(step)
        
        assertNull(duration)
    }
    
    @Test
    fun `formatDuration should format seconds correctly`() {
        assertEquals("1h 30m 45s", DurationCalculator.formatDuration(5445))
        assertEquals("30m 45s", DurationCalculator.formatDuration(1845))
        assertEquals("45s", DurationCalculator.formatDuration(45))
        assertEquals("N/A", DurationCalculator.formatDuration(null))
    }
}

