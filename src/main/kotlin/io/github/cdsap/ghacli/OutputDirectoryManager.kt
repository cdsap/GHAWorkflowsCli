package io.github.cdsap.ghacli

import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object OutputDirectoryManager {
    fun createOutputDirectory(
        owner: String,
        repo: String,
        workflowId: String?
    ): String {
        val outputBaseDir = File("output")
        if (!outputBaseDir.exists()) {
            outputBaseDir.mkdirs()
        }
        
        val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"))
        val sanitizedOwner = sanitizeForFilename(owner)
        val sanitizedRepo = sanitizeForFilename(repo)
        val sanitizedWorkflowId = if (workflowId != null) {
            sanitizeForFilename(workflowId)
        } else {
            "all-workflows"
        }
        
        val folderName = "$sanitizedOwner-$sanitizedRepo-$sanitizedWorkflowId-$timestamp"
        val outputDir = File(outputBaseDir, folderName)
        outputDir.mkdirs()
        
        return outputDir.absolutePath
    }
    
    private fun sanitizeForFilename(name: String): String {
        return name
            .replace(Regex("[^a-zA-Z0-9._-]"), "_")
            .replace(Regex("_{2,}"), "_")
            .trim('_')
            .take(100) // Limit length to avoid filesystem issues
    }
}

