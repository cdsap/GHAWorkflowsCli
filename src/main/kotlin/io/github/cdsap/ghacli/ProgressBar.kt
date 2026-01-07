package io.github.cdsap.ghacli

class ProgressBar {
    private var progress: StringBuilder = StringBuilder(60)
    private var lastPercent = -1
    
    @Synchronized
    fun update(
        done: Int,
        total: Int,
    ) {
        if (total == 0) return
        
        val workchars = charArrayOf('|', '/', '-', '\\')
        val percent = (done * 100 / total).coerceAtMost(100)
        
        // Reset and rebuild progress bar every time
        progress.clear()
        val progressBarLength = (percent / 2).coerceAtMost(50)
        repeat(progressBarLength) {
            progress.append('#')
        }
        
        // Build the progress string
        val spinner = workchars[done % workchars.size]
        val progressLine = String.format("\r%3d%% %s %c", percent, progress, spinner)
        
        // Print and flush immediately
        print(progressLine)
        System.out.flush()
        
        if (done >= total) {
            println()
            System.out.flush()
            progress.clear()
            lastPercent = -1
        }
    }
}

