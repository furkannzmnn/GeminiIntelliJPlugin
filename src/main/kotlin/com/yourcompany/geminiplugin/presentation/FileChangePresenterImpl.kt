package com.yourcompany.geminiplugin.presentation

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.fileEditor.OpenFileDescriptor
import com.intellij.openapi.project.Project
import com.yourcompany.geminiplugin.application.FileChangePresenter
import com.yourcompany.geminiplugin.domain.FileChangeEvent
import com.yourcompany.geminiplugin.domain.FileChangeService
import javax.swing.DefaultListModel

/**
 * Concrete implementation of FileChangePresenter
 */
class FileChangePresenterImpl(
    private val project: Project,
    private val fileChangeService: FileChangeService,
    private val fileChangeListModel: DefaultListModel<com.yourcompany.geminiplugin.FileChangeEntry>
) : FileChangePresenter {
    
    override fun startPromptTracking() {
        fileChangeService.startPromptTracking()
    }
    
    override fun presentFileChange(event: FileChangeEvent) {
        ApplicationManager.getApplication().invokeLater {
            when (event) {
                is FileChangeEvent.Created -> handleFileCreated(event)
                is FileChangeEvent.Updated -> handleFileUpdated(event)
                is FileChangeEvent.Deleted -> handleFileDeleted(event)
            }
        }
    }
    
    private fun handleFileCreated(event: FileChangeEvent.Created) {
        val entry = com.yourcompany.geminiplugin.FileChangeEntry(
            com.yourcompany.geminiplugin.FileChangeEntry.ChangeType.CREATED,
            event.file
        )
        fileChangeListModel.addElement(entry)
        openFileWithStreamEffect(event.file)
    }
    
    private fun handleFileUpdated(event: FileChangeEvent.Updated) {
        val entry = com.yourcompany.geminiplugin.FileChangeEntry(
            com.yourcompany.geminiplugin.FileChangeEntry.ChangeType.UPDATED,
            event.file
        )
        fileChangeListModel.addElement(entry)
        openFileWithDiffEffect(event.file, event.oldContent)
    }
    
    private fun handleFileDeleted(event: FileChangeEvent.Deleted) {
        // Handle file deletion if needed
    }
    
    private fun openFileWithStreamEffect(file: com.intellij.openapi.vfs.VirtualFile) {
        val descriptor = OpenFileDescriptor(project, file)
        descriptor.navigateInEditor(project, true)
        showStreamEffect(file)
    }
    
    private fun openFileWithDiffEffect(file: com.intellij.openapi.vfs.VirtualFile, oldContent: String?) {
        val descriptor = OpenFileDescriptor(project, file)
        descriptor.navigateInEditor(project, true)
        showDiffEffect(file, oldContent)
    }
    
    private fun showStreamEffect(file: com.intellij.openapi.vfs.VirtualFile) {
        com.intellij.openapi.application.ApplicationManager.getApplication().invokeLater {
            try {
                val descriptor = com.intellij.openapi.fileEditor.OpenFileDescriptor(project, file)
                val editor = descriptor.navigateInEditor(project, true)
                val document = com.intellij.openapi.fileEditor.FileDocumentManager.getInstance().getDocument(file) ?: return@invokeLater
                
                val content = document.text
                if (content.isNotEmpty()) {
                    com.intellij.openapi.command.WriteCommandAction.runWriteCommandAction(project) {
                        document.setText("")
                    }
                    
                    kotlinx.coroutines.GlobalScope.launch(kotlinx.coroutines.Dispatchers.Main) {
                        for (i in 1..content.length) {
                            com.intellij.openapi.command.WriteCommandAction.runWriteCommandAction(project) {
                                document.setText(content.substring(0, i))
                            }
                            kotlinx.coroutines.delay(20)
                        }
                    }
                }
            } catch (e: Exception) {
                println("DEBUG: Error in stream effect: ${e.message}")
            }
        }
    }
    
    private fun showDiffEffect(file: com.intellij.openapi.vfs.VirtualFile, oldContent: String?) {
        com.intellij.openapi.application.ApplicationManager.getApplication().invokeLater {
            try {
                val dialog = com.yourcompany.geminiplugin.presentation.CustomDiffDialog(project, file, oldContent)
                dialog.show()
            } catch (e: Exception) {
                println("DEBUG: Error showing diff dialog: ${e.message}")
            }
        }
    }
} 