package io.github.cdsap.ghacli

import io.github.cdsap.ghacli.models.WorkflowRun
import java.time.LocalDate
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

enum class RerunHandling {
    INCLUDE,      // Include both original runs and reruns
    EXCLUDE,      // Exclude reruns, only include original runs (run_attempt == 1)
    ONLY_RERUNS   // Only include reruns (run_attempt > 1)
}

data class FilterCriteria(
    val fromDate: LocalDate? = null,
    val toDate: LocalDate? = null,
    val onlySuccess: Boolean = false,
    val rerunHandling: RerunHandling = RerunHandling.INCLUDE
)

class WorkflowRunFilter {
    fun filterRuns(runs: List<WorkflowRun>, criteria: FilterCriteria): List<WorkflowRun> {
        return runs.filter { run ->
            // Filter by rerun handling
            val isRerun = run.run_attempt > 1
            when (criteria.rerunHandling) {
                RerunHandling.EXCLUDE -> if (isRerun) return@filter false
                RerunHandling.ONLY_RERUNS -> if (!isRerun) return@filter false
                RerunHandling.INCLUDE -> { /* Include all */ }
            }
            
            // Filter by success status
            if (criteria.onlySuccess && run.conclusion != "success") {
                return@filter false
            }
            
            // Filter by date
            val runDate = parseRunDate(run.created_at) ?: return@filter true // Include if we can't parse
            
            when {
                criteria.fromDate != null && criteria.toDate != null -> {
                    runDate.isAfter(criteria.fromDate.minusDays(1)) && 
                    runDate.isBefore(criteria.toDate.plusDays(1))
                }
                criteria.fromDate != null -> runDate.isAfter(criteria.fromDate.minusDays(1))
                criteria.toDate != null -> runDate.isBefore(criteria.toDate.plusDays(1))
                else -> true
            }
        }
    }
    
    fun getFilterDescription(criteria: FilterCriteria): List<String> {
        val descriptions = mutableListOf<String>()
        
        when (criteria.rerunHandling) {
            RerunHandling.EXCLUDE -> descriptions.add("Excluding reruns (only original runs)")
            RerunHandling.ONLY_RERUNS -> descriptions.add("Only reruns")
            RerunHandling.INCLUDE -> { /* No description needed */ }
        }
        
        if (criteria.onlySuccess) {
            descriptions.add("Only successful workflows")
        }
        
        if (criteria.fromDate != null || criteria.toDate != null) {
            val dateRange = when {
                criteria.fromDate != null && criteria.toDate != null -> 
                    "${criteria.fromDate} to ${criteria.toDate}"
                criteria.fromDate != null -> "from ${criteria.fromDate}"
                criteria.toDate != null -> "to ${criteria.toDate}"
                else -> ""
            }
            descriptions.add("Date range: $dateRange")
        }
        
        return descriptions
    }
    
    private fun parseRunDate(dateString: String): LocalDate? {
        return try {
            ZonedDateTime.parse(dateString, DateTimeFormatter.ISO_DATE_TIME).toLocalDate()
        } catch (e: Exception) {
            null
        }
    }
}

