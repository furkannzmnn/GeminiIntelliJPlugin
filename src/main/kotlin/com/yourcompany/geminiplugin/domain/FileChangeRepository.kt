package com.yourcompany.geminiplugin.domain

import com.intellij.openapi.vfs.VirtualFile

/**
 * Repository for managing file change events
 */
interface FileChangeRepository {
    fun saveFileContent(file: VirtualFile, content: String)
    fun getFileContent(file: VirtualFile): String?
    fun clearCache()
    fun isFileInSourceRoot(file: VirtualFile): Boolean
} 