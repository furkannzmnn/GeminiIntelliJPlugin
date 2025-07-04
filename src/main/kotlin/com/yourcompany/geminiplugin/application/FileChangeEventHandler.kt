package com.yourcompany.geminiplugin.application

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.newvfs.events.VFileEvent
import com.yourcompany.geminiplugin.domain.FileChangeEvent
import com.yourcompany.geminiplugin.domain.FileChangeService

/**
 * Handles file change events in the application layer
 */
class FileChangeEventHandler(
    private val fileChangeUseCase: FileChangeUseCase
) {
    
    fun handleBeforeEvent(events: List<VFileEvent>) {
        events.forEach { event ->
            val file = event.file
            if (file != null && file.isInLocalFileSystem) {
                fileChangeUseCase.cacheFileContent(file)
            }
        }
    }
    
    fun handleAfterEvent(events: List<VFileEvent>) {
        events.forEach { event ->
            val file = event.file
            if (file != null && file.isInLocalFileSystem) {
                val eventType = getEventType(event)
                fileChangeUseCase.processFileChange(file, eventType)
            }
        }
    }
    
    private fun getEventType(event: VFileEvent): String {
        return when (event) {
            is com.intellij.openapi.vfs.newvfs.events.VFileCreateEvent -> "CREATED"
            is com.intellij.openapi.vfs.newvfs.events.VFileContentChangeEvent -> "UPDATED"
            is com.intellij.openapi.vfs.newvfs.events.VFileDeleteEvent -> "DELETED"
            else -> "UNKNOWN"
        }
    }
} 