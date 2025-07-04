package com.yourcompany.geminiplugin.application

import com.yourcompany.geminiplugin.domain.FileChangeEvent

/**
 * Interface for presenting file changes to the user
 */
interface FileChangePresenter {
    fun presentFileChange(event: FileChangeEvent)
    fun startPromptTracking()
} 