package com.yourcompany.geminiplugin.domain

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile

/**
 * Domain service for handling file change business logic
 */
class FileChangeService(
    private val repository: FileChangeRepository,
    private val project: Project
) {
    private var promptStartTime: Long = 0
    
    fun startPromptTracking() {
        promptStartTime = System.currentTimeMillis()
        repository.clearCache()
    }
    
    fun shouldTrackFileChange(file: VirtualFile): Boolean {
        return repository.isFileInSourceRoot(file) && 
               System.currentTimeMillis() >= promptStartTime
    }
    
    fun cacheFileContent(file: VirtualFile) {
        val content = getFileContent(file)
        if (content != null) {
            repository.saveFileContent(file, content)
        }
    }
    
    fun getOldContent(file: VirtualFile): String? {
        return repository.getFileContent(file)
    }
    
    private fun getFileContent(file: VirtualFile): String? {
        return try {
            val document = com.intellij.openapi.fileEditor.FileDocumentManager.getInstance().getDocument(file)
            document?.text
        } catch (e: Exception) {
            null
        }
    }
} 