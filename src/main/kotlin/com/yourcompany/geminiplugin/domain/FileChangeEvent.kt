package com.yourcompany.geminiplugin.domain

import com.intellij.openapi.vfs.VirtualFile

/**
 * Represents a file change event in the domain
 */
sealed class FileChangeEvent {
    abstract val file: VirtualFile
    abstract val timestamp: Long
    
    data class Created(
        override val file: VirtualFile,
        override val timestamp: Long = System.currentTimeMillis()
    ) : FileChangeEvent()
    
    data class Updated(
        override val file: VirtualFile,
        override val timestamp: Long = System.currentTimeMillis(),
        val oldContent: String? = null,
        val newContent: String? = null
    ) : FileChangeEvent()
    
    data class Deleted(
        override val file: VirtualFile,
        override val timestamp: Long = System.currentTimeMillis()
    ) : FileChangeEvent()
} 