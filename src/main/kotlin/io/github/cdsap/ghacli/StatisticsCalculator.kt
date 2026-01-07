package io.github.cdsap.ghacli

object StatisticsCalculator {
    fun calculateMean(values: List<Long>): Double {
        if (values.isEmpty()) return 0.0
        return values.average()
    }

    fun calculateMedian(values: List<Long>): Double {
        if (values.isEmpty()) return 0.0
        val sorted = values.sorted()
        val size = sorted.size
        return if (size % 2 == 0) {
            (sorted[size / 2 - 1] + sorted[size / 2]) / 2.0
        } else {
            sorted[size / 2].toDouble()
        }
    }

    fun calculateP90(values: List<Long>): Double {
        if (values.isEmpty()) return 0.0
        val sorted = values.sorted()
        val index = (sorted.size * 0.9).toInt().coerceAtMost(sorted.size - 1)
        return sorted[index].toDouble()
    }

    fun formatSeconds(seconds: Double): String {
        val hours = (seconds / 3600).toInt()
        val minutes = ((seconds % 3600) / 60).toInt()
        val secs = (seconds % 60).toInt()
        
        return when {
            hours > 0 -> "${hours}h ${minutes}m ${secs}s"
            minutes > 0 -> "${minutes}m ${secs}s"
            else -> "${secs}s"
        }
    }
}

