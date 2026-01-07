package io.github.cdsap.ghacli

class StatisticsReporter {
    fun printWorkflowStatistics(durations: List<Long>) {
        println("=".repeat(60))
        println("WORKFLOW DURATION STATISTICS")
        println("=".repeat(60))
        
        if (durations.isEmpty()) {
            println("No workflow duration data available")
            return
        }
        
        val mean = StatisticsCalculator.calculateMean(durations)
        val median = StatisticsCalculator.calculateMedian(durations)
        val p90 = StatisticsCalculator.calculateP90(durations)
        
        println("Mean:   ${StatisticsCalculator.formatSeconds(mean)} (${mean.toLong()}s)")
        println("Median: ${StatisticsCalculator.formatSeconds(median)} (${median.toLong()}s)")
        println("P90:    ${StatisticsCalculator.formatSeconds(p90)} (${p90.toLong()}s)")
        println("Count:  ${durations.size}")
    }
    
    fun printJobStatistics(durations: List<Long>) {
        println("\n" + "=".repeat(60))
        println("JOB DURATION STATISTICS")
        println("=".repeat(60))
        
        if (durations.isEmpty()) {
            println("No job duration data available")
            return
        }
        
        val mean = StatisticsCalculator.calculateMean(durations)
        val median = StatisticsCalculator.calculateMedian(durations)
        val p90 = StatisticsCalculator.calculateP90(durations)
        
        println("Mean:   ${StatisticsCalculator.formatSeconds(mean)} (${mean.toLong()}s)")
        println("Median: ${StatisticsCalculator.formatSeconds(median)} (${median.toLong()}s)")
        println("P90:    ${StatisticsCalculator.formatSeconds(p90)} (${p90.toLong()}s)")
        println("Count:  ${durations.size}")
    }
    
    fun printStepStatistics(durations: List<Long>) {
        println("\n" + "=".repeat(60))
        println("STEP DURATION STATISTICS")
        println("=".repeat(60))
        
        if (durations.isEmpty()) {
            println("No step duration data available")
            return
        }
        
        val mean = StatisticsCalculator.calculateMean(durations)
        val median = StatisticsCalculator.calculateMedian(durations)
        val p90 = StatisticsCalculator.calculateP90(durations)
        
        println("Mean:   ${StatisticsCalculator.formatSeconds(mean)} (${mean.toLong()}s)")
        println("Median: ${StatisticsCalculator.formatSeconds(median)} (${median.toLong()}s)")
        println("P90:    ${StatisticsCalculator.formatSeconds(p90)} (${p90.toLong()}s)")
        println("Count:  ${durations.size}")
    }
    
    fun printFilterInfo(
        totalFound: Int, 
        filteredCount: Int, 
        filterDescriptions: List<String>,
        rerunCount: Int = 0,
        originalRunCount: Int = 0
    ) {
        if (filterDescriptions.isNotEmpty()) {
            println("Filters applied:")
            filterDescriptions.forEach { println("  - $it") }
            println("Total workflow runs found: $totalFound")
            println("Workflow runs after filters: $filteredCount")
        } else {
            println("Total workflow runs found: $totalFound")
        }
        
        if (rerunCount > 0 || originalRunCount > 0) {
            println("  - Original runs: $originalRunCount")
            println("  - Reruns: $rerunCount")
        }
        
        println("Analyzing $filteredCount workflow run(s)...\n")
    }
    
    fun printExportInfo(
        overallCsv: String,
        jobFiles: List<String>,
        jobsSummary: String,
        stepsSummary: String,
        outputDir: String
    ) {
        println("\n" + "=".repeat(60))
        println("All metrics exported to: $outputDir")
        println("\nFiles created:")
        println("  - $overallCsv")
        println("Per-job metrics (including all steps):")
        jobFiles.forEach { println("  - $it") }
        println("\nSummary files:")
        println("  - $jobsSummary (jobs summary)")
        println("  - $stepsSummary (steps summary)")
        println("=".repeat(60))
    }
}

