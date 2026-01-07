package io.github.cdsap.ghacli

import io.github.cdsap.ghacli.models.WorkflowRun
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import java.time.LocalDate

class WorkflowRunFilterTest {
    
    private val filter = WorkflowRunFilter()
    
    @Test
    fun `filterRuns should return all runs when no criteria specified`() {
        val runs = listOf(
            createRun("2024-01-15T10:00:00Z", "success"),
            createRun("2024-01-16T10:00:00Z", "failure")
        )
        val criteria = FilterCriteria()
        
        val filtered = filter.filterRuns(runs, criteria)
        
        assertEquals(2, filtered.size)
    }
    
    @Test
    fun `filterRuns should filter by success status`() {
        val runs = listOf(
            createRun("2024-01-15T10:00:00Z", "success"),
            createRun("2024-01-16T10:00:00Z", "failure"),
            createRun("2024-01-17T10:00:00Z", "success")
        )
        val criteria = FilterCriteria(onlySuccess = true)
        
        val filtered = filter.filterRuns(runs, criteria)
        
        assertEquals(2, filtered.size)
        assertTrue(filtered.all { it.conclusion == "success" })
    }
    
    @Test
    fun `filterRuns should filter by fromDate`() {
        val runs = listOf(
            createRun("2024-01-15T10:00:00Z", "success"),
            createRun("2024-01-20T10:00:00Z", "success"),
            createRun("2024-01-25T10:00:00Z", "success")
        )
        val criteria = FilterCriteria(fromDate = LocalDate.of(2024, 1, 18))
        
        val filtered = filter.filterRuns(runs, criteria)
        
        assertEquals(2, filtered.size)
    }
    
    @Test
    fun `filterRuns should filter by toDate`() {
        val runs = listOf(
            createRun("2024-01-15T10:00:00Z", "success"),
            createRun("2024-01-20T10:00:00Z", "success"),
            createRun("2024-01-25T10:00:00Z", "success")
        )
        val criteria = FilterCriteria(toDate = LocalDate.of(2024, 1, 22))
        
        val filtered = filter.filterRuns(runs, criteria)
        
        assertEquals(2, filtered.size)
    }
    
    @Test
    fun `filterRuns should filter by date range`() {
        val runs = listOf(
            createRun("2024-01-15T10:00:00Z", "success"),
            createRun("2024-01-20T10:00:00Z", "success"),
            createRun("2024-01-25T10:00:00Z", "success")
        )
        val criteria = FilterCriteria(
            fromDate = LocalDate.of(2024, 1, 18),
            toDate = LocalDate.of(2024, 1, 22)
        )
        
        val filtered = filter.filterRuns(runs, criteria)
        
        assertEquals(1, filtered.size)
    }
    
    @Test
    fun `filterRuns should combine success and date filters`() {
        val runs = listOf(
            createRun("2024-01-15T10:00:00Z", "success"),
            createRun("2024-01-20T10:00:00Z", "failure"),
            createRun("2024-01-25T10:00:00Z", "success")
        )
        val criteria = FilterCriteria(
            fromDate = LocalDate.of(2024, 1, 18),
            onlySuccess = true
        )
        
        val filtered = filter.filterRuns(runs, criteria)
        
        assertEquals(1, filtered.size)
        assertEquals("success", filtered.first().conclusion)
    }
    
    @Test
    fun `getFilterDescription should return empty list when no filters`() {
        val criteria = FilterCriteria()
        val descriptions = filter.getFilterDescription(criteria)
        
        assertTrue(descriptions.isEmpty())
    }
    
    @Test
    fun `getFilterDescription should include success filter description`() {
        val criteria = FilterCriteria(onlySuccess = true)
        val descriptions = filter.getFilterDescription(criteria)
        
        assertEquals(1, descriptions.size)
        assertTrue(descriptions.contains("Only successful workflows"))
    }
    
    @Test
    fun `filterRuns should exclude reruns when EXCLUDE is specified`() {
        val runs = listOf(
            createRun("2024-01-15T10:00:00Z", "success", runAttempt = 1),
            createRun("2024-01-16T10:00:00Z", "success", runAttempt = 2),
            createRun("2024-01-17T10:00:00Z", "success", runAttempt = 1)
        )
        val criteria = FilterCriteria(rerunHandling = RerunHandling.EXCLUDE)
        
        val filtered = filter.filterRuns(runs, criteria)
        
        assertEquals(2, filtered.size)
        assertTrue(filtered.all { it.run_attempt == 1 })
    }
    
    @Test
    fun `filterRuns should only include reruns when ONLY_RERUNS is specified`() {
        val runs = listOf(
            createRun("2024-01-15T10:00:00Z", "success", runAttempt = 1),
            createRun("2024-01-16T10:00:00Z", "success", runAttempt = 2),
            createRun("2024-01-17T10:00:00Z", "success", runAttempt = 3)
        )
        val criteria = FilterCriteria(rerunHandling = RerunHandling.ONLY_RERUNS)
        
        val filtered = filter.filterRuns(runs, criteria)
        
        assertEquals(2, filtered.size)
        assertTrue(filtered.all { it.run_attempt > 1 })
    }
    
    @Test
    fun `getFilterDescription should include rerun handling description`() {
        val criteria = FilterCriteria(rerunHandling = RerunHandling.EXCLUDE)
        val descriptions = filter.getFilterDescription(criteria)
        
        assertEquals(1, descriptions.size)
        assertTrue(descriptions.contains("Excluding reruns (only original runs)"))
    }
    
    @Test
    fun `getFilterDescription should include date range description`() {
        val criteria = FilterCriteria(
            fromDate = LocalDate.of(2024, 1, 1),
            toDate = LocalDate.of(2024, 12, 31)
        )
        val descriptions = filter.getFilterDescription(criteria)
        
        assertEquals(1, descriptions.size)
        assertTrue(descriptions.any { it.contains("2024-01-01 to 2024-12-31") })
    }
    
    private fun createRun(createdAt: String, conclusion: String, runAttempt: Int = 1): WorkflowRun {
        return WorkflowRun(
            id = 1,
            name = "Test",
            status = "completed",
            conclusion = conclusion,
            created_at = createdAt,
            updated_at = createdAt,
            run_started_at = createdAt,
            head_branch = "main",
            workflow_id = 1,
            html_url = "https://github.com",
            run_number = 1,
            jobs_url = "https://github.com",
            run_attempt = runAttempt
        )
    }
}

