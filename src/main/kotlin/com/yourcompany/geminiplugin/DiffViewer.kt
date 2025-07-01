
package com.yourcompany.geminiplugin

import com.intellij.diff.DiffContentFactory
import com.intellij.diff.DiffManager
import com.intellij.diff.requests.SimpleDiffRequest
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem

object DiffViewer {
    fun showDiff(project: Project, originalContent: String, newContent: String, filePath: String) {
        ApplicationManager.getApplication().invokeLater {
            val virtualFile = LocalFileSystem.getInstance().findFileByPath(filePath)
            if (virtualFile == null) {
                // TODO: Handle file not found
                println("File not found: $filePath")
                return@invokeLater
            }

            val document = FileDocumentManager.getInstance().getDocument(virtualFile)
            if (document == null) {
                // TODO: Handle document not found
                println("Document not found for file: $filePath")
                return@invokeLater
            }

            val contentFactory = DiffContentFactory.getInstance()
            val original = contentFactory.create(project, document)
            val new = contentFactory.create(newContent)

            val request = SimpleDiffRequest("Gemini AI Suggestion", original, new, "Original", "Gemini")

            // The user can apply changes directly from the diff view
            DiffManager.getInstance().showDiff(project, request)

            // Example of how to apply the change programmatically (e.g., on a button click)
            // WriteCommandAction.runWriteCommandAction(project) {
            //     document.setText(newContent)
            // }
        }
    }
}
