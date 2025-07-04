package com.yourcompany.geminiplugin.application

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.yourcompany.geminiplugin.domain.FileChangeEvent
import com.yourcompany.geminiplugin.domain.FileChangeService

/**
 * Use Case for handling file change operations
 */
class FileChangeUseCase(
    private val fileChangeService: FileChangeService,
    private val fileChangePresenter: FileChangePresenter
) {
    
    fun startTrackingFileChanges() {
        fileChangeService.startPromptTracking()
    }
    
    fun processFileChange(file: VirtualFile, eventType: String) {
        if (!fileChangeService.shouldTrackFileChange(file)) {
            return
        }
        
        val event = when (eventType) {
            "CREATED" -> FileChangeEvent.Created(file)
            "UPDATED" -> {
                val oldContent = fileChangeService.getOldContent(file)
                FileChangeEvent.Updated(file, oldContent = oldContent)
            }
            "DELETED" -> FileChangeEvent.Deleted(file)
            else -> return
        }
        
        fileChangePresenter.presentFileChange(event)
    }
    
    fun cacheFileContent(file: VirtualFile) {
        if (fileChangeService.shouldTrackFileChange(file)) {
            fileChangeService.cacheFileContent(file)
        }
    }
} 