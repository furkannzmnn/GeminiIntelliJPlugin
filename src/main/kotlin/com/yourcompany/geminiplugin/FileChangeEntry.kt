package com.yourcompany.geminiplugin

import com.intellij.openapi.vfs.VirtualFile

data class FileChangeEntry(
    val type: ChangeType,
    val virtualFile: VirtualFile
) {
    enum class ChangeType { CREATED, UPDATED }
}
