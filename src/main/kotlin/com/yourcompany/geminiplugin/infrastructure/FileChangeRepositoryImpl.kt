package com.yourcompany.geminiplugin.infrastructure

import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.openapi.vfs.VirtualFile
import com.yourcompany.geminiplugin.domain.FileChangeRepository

/**
 * Concrete implementation of FileChangeRepository
 */
class FileChangeRepositoryImpl(
    private val project: Project
) : FileChangeRepository {
    
    private val fileContentCache = mutableMapOf<String, String>()
    
    override fun saveFileContent(file: VirtualFile, content: String) {
        fileContentCache[file.path] = content
    }
    
    override fun getFileContent(file: VirtualFile): String? {
        return fileContentCache[file.path]
    }
    
    override fun clearCache() {
        fileContentCache.clear()
    }
    
    override fun isFileInSourceRoot(file: VirtualFile): Boolean {
        val sourceRoots = ProjectRootManager.getInstance(project).contentSourceRoots
        return sourceRoots.any { VfsUtilCore.isAncestor(it, file, true) }
    }
} 