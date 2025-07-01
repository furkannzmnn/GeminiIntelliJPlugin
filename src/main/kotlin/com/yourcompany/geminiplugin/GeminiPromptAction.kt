
package com.yourcompany.geminiplugin

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile

class GeminiPromptAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val project: Project? = e.project
        if (project == null) return

        // Gather context: selected files in Project View
        val selectedFiles: List<VirtualFile>? = e.getData(CommonDataKeys.VIRTUAL_FILE_ARRAY)?.toList()
        val context = selectedFiles?.joinToString(" ") { it.path } ?: ""

        val dialog = PromptDialog(project, context)
        dialog.show()
    }

    override fun update(e: AnActionEvent) {
        val project = e.project
        e.presentation.isEnabledAndVisible = project != null
    }
}
