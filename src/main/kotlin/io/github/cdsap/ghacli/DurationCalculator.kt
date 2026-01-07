package io.github.cdsap.ghacli

import io.github.cdsap.ghacli.models.Job
import io.github.cdsap.ghacli.models.JobStep
import io.github.cdsap.ghacli.models.WorkflowRun
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

object DurationCalculator {
    fun calculateRunDuration(run: WorkflowRun): Long? {
        val startedAt = run.run_started_at ?: run.created_at
        val updatedAt = run.updated_at

        return try {
            val start = ZonedDateTime.parse(startedAt, DateTimeFormatter.ISO_DATE_TIME)
            val end = ZonedDateTime.parse(updatedAt, DateTimeFormatter.ISO_DATE_TIME)
            ChronoUnit.SECONDS.between(start, end)
        } catch (e: Exception) {
            null
        }
    }

    fun calculateJobDuration(job: Job): Long? {
        val startedAt = job.started_at ?: return null
        val completedAt = job.completed_at ?: return null

        return try {
            val start = ZonedDateTime.parse(startedAt, DateTimeFormatter.ISO_DATE_TIME)
            val end = ZonedDateTime.parse(completedAt, DateTimeFormatter.ISO_DATE_TIME)
            ChronoUnit.SECONDS.between(start, end)
        } catch (e: Exception) {
            null
        }
    }

    fun calculateStepDuration(step: JobStep): Long? {
        val startedAt = step.started_at ?: return null
        val completedAt = step.completed_at ?: return null

        return try {
            val start = ZonedDateTime.parse(startedAt, DateTimeFormatter.ISO_DATE_TIME)
            val end = ZonedDateTime.parse(completedAt, DateTimeFormatter.ISO_DATE_TIME)
            ChronoUnit.SECONDS.between(start, end)
        } catch (e: Exception) {
            null
        }
    }

    fun formatDuration(seconds: Long?): String {
        if (seconds == null) return "N/A"

        val hours = seconds / 3600
        val minutes = (seconds % 3600) / 60
        val secs = seconds % 60

        return when {
            hours > 0 -> "${hours}h ${minutes}m ${secs}s"
            minutes > 0 -> "${minutes}m ${secs}s"
            else -> "${secs}s"
        }
    }
}

