package io.github.cdsap.ghacli.models

import kotlinx.serialization.Serializable

@Serializable
data class WorkflowRun(
    val id: Long,
    val name: String,
    val status: String,
    val conclusion: String?,
    val created_at: String,
    val updated_at: String,
    val run_started_at: String?,
    val head_branch: String?,
    val workflow_id: Long,
    val html_url: String,
    val run_number: Long,
    val jobs_url: String,
    val run_attempt: Int = 1 // 1 for original run, 2+ for reruns
)

@Serializable
data class WorkflowRunsResponse(
    val total_count: Int,
    val workflow_runs: List<WorkflowRun>
)

@Serializable
data class Job(
    val id: Long,
    val name: String,
    val status: String,
    val conclusion: String?,
    val started_at: String?,
    val completed_at: String?,
    val steps: List<JobStep>?
)

@Serializable
data class JobStep(
    val name: String,
    val status: String,
    val conclusion: String?,
    val number: Int,
    val started_at: String?,
    val completed_at: String?
)

@Serializable
data class JobsResponse(
    val total_count: Int,
    val jobs: List<Job>
)

