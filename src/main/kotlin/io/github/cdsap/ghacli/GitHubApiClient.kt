package io.github.cdsap.ghacli

import io.github.cdsap.ghacli.models.JobsResponse
import io.github.cdsap.ghacli.models.WorkflowRunsResponse
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class GitHubApiClient(private val token: String? = null) {
    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
    }

    private val baseUrl = "https://api.github.com"

    suspend fun getWorkflowRuns(
        owner: String,
        repo: String,
        workflowId: String? = null,
        branch: String? = null,
        perPage: Int = 30,
        createdFrom: LocalDate? = null,
        createdTo: LocalDate? = null
    ): WorkflowRunsResponse {
        val url = buildString {
            append("$baseUrl/repos/$owner/$repo/actions/workflows/$workflowId/runs")
            val params = mutableListOf<String>()
            if (branch != null) params.add("branch=$branch")
            params.add("per_page=$perPage")

            // Add date filters - GitHub API uses created parameter with >= and <= operators
            // Format: created=YYYY-MM-DD..YYYY-MM-DD
            if (createdFrom != null || createdTo != null) {
                val dateRange = when {
                    createdFrom != null && createdTo != null -> {
                        "${createdFrom.format(DateTimeFormatter.ISO_DATE)}..${createdTo.format(DateTimeFormatter.ISO_DATE)}"
                    }
                    createdFrom != null -> {
                        ">=${createdFrom.format(DateTimeFormatter.ISO_DATE)}"
                    }
                    createdTo != null -> {
                        "<=${createdTo.format(DateTimeFormatter.ISO_DATE)}"
                    }
                    else -> ""
                }
                if (dateRange.isNotEmpty()) {
                    params.add("created=$dateRange")
                }
            }

            if (params.isNotEmpty()) {
                append("?${params.joinToString("&")}")
            }
        }

        return client.get(url) {
            headers {
                append(HttpHeaders.Accept, "application/vnd.github.v3+json")
                if (token != null) {
                    append(HttpHeaders.Authorization, "token $token")
                }
            }
        }.body()
    }

    suspend fun getWorkflowRunJobs(
        owner: String,
        repo: String,
        runId: Long
    ): JobsResponse {
        val url = "$baseUrl/repos/$owner/$repo/actions/runs/$runId/jobs"

        return client.get(url) {
            headers {
                append(HttpHeaders.Accept, "application/vnd.github.v3+json")
                if (token != null) {
                    append(HttpHeaders.Authorization, "token $token")
                }
            }
        }.body()
    }

    fun close() {
        client.close()
    }
}

