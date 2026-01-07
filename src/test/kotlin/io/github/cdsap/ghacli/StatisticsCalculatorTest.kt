package io.github.cdsap.ghacli

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class StatisticsCalculatorTest {
    
    @Test
    fun `calculateMean should return correct average`() {
        val values = listOf(10L, 20L, 30L, 40L, 50L)
        val mean = StatisticsCalculator.calculateMean(values)
        
        assertEquals(30.0, mean, 0.01)
    }
    
    @Test
    fun `calculateMean should return 0 for empty list`() {
        val mean = StatisticsCalculator.calculateMean(emptyList())
        
        assertEquals(0.0, mean)
    }
    
    @Test
    fun `calculateMedian should return correct median for odd number of elements`() {
        val values = listOf(10L, 20L, 30L, 40L, 50L)
        val median = StatisticsCalculator.calculateMedian(values)
        
        assertEquals(30.0, median, 0.01)
    }
    
    @Test
    fun `calculateMedian should return correct median for even number of elements`() {
        val values = listOf(10L, 20L, 30L, 40L)
        val median = StatisticsCalculator.calculateMedian(values)
        
        assertEquals(25.0, median, 0.01) // (20 + 30) / 2
    }
    
    @Test
    fun `calculateMedian should return 0 for empty list`() {
        val median = StatisticsCalculator.calculateMedian(emptyList())
        
        assertEquals(0.0, median)
    }
    
    @Test
    fun `calculateP90 should return correct 90th percentile`() {
        val values = (1..100).map { it.toLong() }
        val p90 = StatisticsCalculator.calculateP90(values)
        
        // For 100 elements, 90th percentile index is (100 * 0.9) = 90, which is the 91st element (0-indexed)
        // So we expect value 91, not 90
        assertEquals(91.0, p90, 0.01)
    }
    
    @Test
    fun `calculateP90 should return last element for small lists`() {
        val values = listOf(10L, 20L, 30L)
        val p90 = StatisticsCalculator.calculateP90(values)
        
        assertEquals(30.0, p90, 0.01)
    }
    
    @Test
    fun `calculateP90 should return 0 for empty list`() {
        val p90 = StatisticsCalculator.calculateP90(emptyList())
        
        assertEquals(0.0, p90)
    }
    
    @Test
    fun `formatSeconds should format correctly`() {
        assertEquals("1h 30m 45s", StatisticsCalculator.formatSeconds(5445.0))
        assertEquals("30m 45s", StatisticsCalculator.formatSeconds(1845.0))
        assertEquals("45s", StatisticsCalculator.formatSeconds(45.0))
        assertEquals("0s", StatisticsCalculator.formatSeconds(0.0))
    }
}

