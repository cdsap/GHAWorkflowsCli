package io.github.cdsap.ghacli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.main
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.int
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Semaphore as CoroutineSemaphore
import kotlinx.coroutines.withContext
import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.concurrent.atomic.AtomicInteger

fun main(args: Array<String>) {
    GHAClI().main(args)
}

class GHAClI : CliktCommand() {
    private val token by option().required()
    private val user by option()
    private val repo by option().required()
    private val owner by option().required()
    private val branch by option()
    private val workflowId by option().required()
    private val maxBuilds by option().int().default(10)
    private val fromDate by option(
        help = "Filter workflow runs from this date (format: YYYY-MM-DD, e.g., 2024-01-01)"
    )
    private val toDate by option(
        help = "Filter workflow runs to this date (format: YYYY-MM-DD, e.g., 2024-12-31)"
    )
    private val onlySuccess by option(
        help = "Only process successful workflow runs"
    ).flag()
    private val excludeReruns by option(
        help = "Exclude reruns, only process original workflow runs"
    ).flag()
    private val onlyReruns by option(
        help = "Only process reruns (exclude original runs)"
    ).flag()
    private val concurrentCalls by option(
        help = "Number of concurrent API calls (default: 8)"
    ).int().default(8)

    override fun run() {
        val apiClient = GitHubApiClient(token)
        val filter = WorkflowRunFilter()
        val collector = MetricsCollector()
        val reporter = StatisticsReporter()
        val aggregator = MetricsAggregator()
        runBlocking {
            // Parse dates if provided
            val fromDateParsed = parseDate(fromDate, "from-date")
            val toDateParsed = parseDate(toDate, "to-date")

            // Fetch workflow runs
            val response = apiClient.getWorkflowRuns(
                owner = owner,
                repo = repo,
                branch = branch,
                workflowId = workflowId,
                perPage = maxBuilds,
                createdFrom = fromDateParsed,
                createdTo = toDateParsed
            )

            if (response.total_count == 0 && response.workflow_runs.isEmpty()) {
                echo("No workflow runs found matching the specified criteria.")
                return@runBlocking
            }
            // Determine rerun handling
            val rerunHandling = when {
                excludeReruns && onlyReruns -> {
                    echo("Error: Cannot use both --exclude-reruns and --only-reruns", err = true)
                    return@runBlocking
                }
                excludeReruns -> RerunHandling.EXCLUDE
                onlyReruns -> RerunHandling.ONLY_RERUNS
                else -> RerunHandling.INCLUDE
            }

            // Filter runs
            val filterCriteria = FilterCriteria(
                fromDate = fromDateParsed,
                toDate = toDateParsed,
                onlySuccess = onlySuccess,
                rerunHandling = rerunHandling
            )
            val filteredRuns = filter.filterRuns(response.workflow_runs, filterCriteria)
            val filterDescriptions = filter.getFilterDescription(filterCriteria)

            // Display build date range before starting job collection
            if (filteredRuns.isNotEmpty()) {
                val sortedRuns = filteredRuns.sortedBy {
                    try {
                        java.time.ZonedDateTime.parse(it.created_at, java.time.format.DateTimeFormatter.ISO_DATE_TIME)
                    } catch (e: Exception) {
                        java.time.ZonedDateTime.now().minusYears(100) // Put unparseable dates at the beginning
                    }
                }

                val firstRun = sortedRuns.first()
                val lastRun = sortedRuns.last()

                val firstDate = try {
                    java.time.ZonedDateTime.parse(firstRun.created_at, java.time.format.DateTimeFormatter.ISO_DATE_TIME)
                        .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                } catch (e: Exception) {
                    firstRun.created_at
                }

                val lastDate = try {
                    java.time.ZonedDateTime.parse(lastRun.created_at, java.time.format.DateTimeFormatter.ISO_DATE_TIME)
                        .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                } catch (e: Exception) {
                    lastRun.created_at
                }

                println("\n" + "=".repeat(60))
                println("BUILD DATE RANGE")
                println("=".repeat(60))
                println("First build:  $firstDate")
                println("Last build:   $lastDate")
                println("=".repeat(60))
                println()
            }

            // Collect metrics with progress bar and concurrent calls
            val collectedMetrics = if (filteredRuns.isNotEmpty()) {
                println("Collecting metrics from ${filteredRuns.size} workflow run(s) with $concurrentCalls concurrent calls...")
                val progressBar = ProgressBar()
                // Show initial progress bar at 0%
                progressBar.update(0, filteredRuns.size)

                val semaphore = CoroutineSemaphore(concurrentCalls)
                val processedCount = AtomicInteger(0)
                val totalRuns = filteredRuns.size

                // Collect jobs concurrently with semaphore
                val jobsMap = coroutineScope {
                    filteredRuns.map { run ->
                        async {
                            semaphore.acquire()
                            try {
                                val jobsResponse = apiClient.getWorkflowRunJobs(owner, repo, run.id)
                                val currentCount = processedCount.incrementAndGet()
                                // Update progress bar (thread-safe via @Synchronized)
                                progressBar.update(currentCount, totalRuns)
                                Pair(run, jobsResponse.jobs)
                            } finally {
                                semaphore.release()
                            }
                        }
                    }.awaitAll()
                }.toMap()

                // Now collect metrics with the jobs we fetched
                collector.collectMetrics(filteredRuns) { run ->
                    jobsMap[run] ?: emptyList()
                }
            } else {
                collector.collectMetrics(emptyList()) { emptyList() }
            }

            reporter.printFilterInfo(
                response.total_count,
                filteredRuns.size,
                filterDescriptions,
                collectedMetrics.rerunCount,
                collectedMetrics.originalRunCount
            )

            // Print statistics
            reporter.printWorkflowStatistics(collectedMetrics.workflowDurations)
            reporter.printJobStatistics(collectedMetrics.jobDurations)
            reporter.printStepStatistics(collectedMetrics.stepDurations)

            // Aggregate and export metrics
            val allJobMetrics = aggregator.aggregateJobMetrics(collectedMetrics)

            // Create output directory with format: owner-repo-workflowId-timestamp
            val outputDir = OutputDirectoryManager.createOutputDirectory(owner, repo, workflowId)

            val exportResult = aggregator.exportAllMetrics(
                collectedMetrics,
                allJobMetrics,
                filteredRuns.size,
                outputDir
            )

            reporter.printExportInfo(
                overallCsv = exportResult.overallCsv,
                jobFiles = exportResult.jobFiles,
                jobsSummary = exportResult.jobsSummary,
                stepsSummary = exportResult.stepsSummary,
                outputDir = outputDir
            )
        }
        apiClient.close()
    }

    private fun parseDate(dateString: String?, paramName: String): LocalDate? {
        return dateString?.let {
            try {
                LocalDate.parse(it, DateTimeFormatter.ISO_DATE)
            } catch (e: Exception) {
                echo("Error: Invalid $paramName format. Use YYYY-MM-DD (e.g., 2024-01-01)", err = true)
                null
            }
        }
    }
}
