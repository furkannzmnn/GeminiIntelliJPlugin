package com.yourcompany.geminiplugin

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.components.JBTextArea
import com.intellij.ui.components.JBLabel
import com.intellij.util.ui.JBUI
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileEvent
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.openapi.vfs.newvfs.BulkFileListener
import com.intellij.openapi.application.ApplicationManager
import javax.swing.JPanel
import javax.swing.JButton
import java.awt.BorderLayout
import java.awt.FlowLayout
import javax.swing.DefaultListModel
import com.intellij.ui.components.JBList
import javax.swing.ListCellRenderer
import java.awt.Component
import javax.swing.JList
import com.intellij.ui.JBColor
import com.intellij.icons.AllIcons
import javax.swing.JLabel
import java.awt.Font
import com.intellij.openapi.fileEditor.OpenFileDescriptor
import com.intellij.openapi.vfs.VirtualFileListener
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent

class GeminiToolWindowFactory : ToolWindowFactory {

    private val rawOutputTextArea = JBTextArea(10, 50).apply { isEditable = false }

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val mainPanel = JPanel(BorderLayout())

        // Input Panel
        val inputPanel = JPanel(BorderLayout())
        val promptLabel = JBLabel("Enter your prompt:")
        val promptTextArea = JBTextArea(5, 50)
        val promptScrollPane = JBScrollPane(promptTextArea)
        inputPanel.add(promptLabel, BorderLayout.NORTH)
        inputPanel.add(promptScrollPane, BorderLayout.CENTER)

        // Buttons panel
        val buttonPanel = JPanel(FlowLayout(FlowLayout.RIGHT))
        val submitButton = JButton("Submit")
        buttonPanel.add(submitButton)
        inputPanel.add(buttonPanel, BorderLayout.SOUTH)

        mainPanel.add(inputPanel, BorderLayout.NORTH)

        // Streamed Output Panel (for file paths)
        val streamedOutputListModel = DefaultListModel<FileChangeEntry>()
        val streamedOutputList = JBList(streamedOutputListModel).apply {
            cellRenderer = FileChangeEntryCellRenderer(project)
        }
        val streamedOutputScrollPane = JBScrollPane(streamedOutputList)
        val streamedOutputLabel = JBLabel("Detected Files:")
        val streamedOutputPanel = JPanel(BorderLayout())
        streamedOutputPanel.add(streamedOutputLabel, BorderLayout.NORTH)
        streamedOutputPanel.add(streamedOutputScrollPane, BorderLayout.CENTER)

        mainPanel.add(streamedOutputPanel, BorderLayout.CENTER)

        // Raw Output Panel (for full response)
        val rawOutputLabel = JBLabel("Full Gemini AI Response:")
        val rawOutputScrollPane = JBScrollPane(rawOutputTextArea)
        val rawOutputPanel = JPanel(BorderLayout())
        rawOutputPanel.add(rawOutputLabel, BorderLayout.NORTH)
        rawOutputPanel.add(rawOutputScrollPane, BorderLayout.CENTER)

        mainPanel.add(rawOutputPanel, BorderLayout.SOUTH)

        // Add padding to the main panel
        mainPanel.border = JBUI.Borders.empty(10)

        // Register VirtualFileListener
        val fileListener = object : VirtualFileListener {
            override fun fileCreated(event: VirtualFileEvent) {
                if (event.file.isInLocalFileSystem) {
                    ApplicationManager.getApplication().invokeLater {
                        streamedOutputListModel.addElement(FileChangeEntry(FileChangeEntry.ChangeType.CREATED, event.file))
                    }
                }
            }

            override fun contentsChanged(event: VirtualFileEvent) {
                if (event.file.isInLocalFileSystem) {
                    ApplicationManager.getApplication().invokeLater {
                        streamedOutputListModel.addElement(FileChangeEntry(FileChangeEntry.ChangeType.UPDATED, event.file))
                    }
                }
            }
        }
        VirtualFileManager.getInstance().addVirtualFileListener(fileListener)

        // Add MouseListener to open files on click
        streamedOutputList.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) {
                if (e.clickCount == 2) { // Double-click to open
                    val selectedIndex = streamedOutputList.selectedIndex
                    if (selectedIndex != -1) {
                        val entry = streamedOutputListModel.getElementAt(selectedIndex)
                        OpenFileDescriptor(project, entry.virtualFile).navigate(true)
                    }
                }
            }
        })

        submitButton.addActionListener {
            val prompt = promptTextArea.text
            if (prompt.isNotBlank()) {
                rawOutputTextArea.text = ""
                streamedOutputListModel.clear()

                val editor = FileEditorManager.getInstance(project).selectedTextEditor
                val currentFile: VirtualFile? = editor?.virtualFile
                val context = currentFile?.path ?: ""

                val geminiCliService = GeminiCliService.getInstance()
                geminiCliService.execute(project, prompt, context,
                    onOutput = { output ->
                        rawOutputTextArea.append(output + "\n")
                    }
                )
                com.yourcompany.geminiplugin.settings.GeminiSettingsState.getInstance().addPromptToHistory(prompt)
            }
        }

        val content = ContentFactory.getInstance().createContent(mainPanel, "Gemini AI", false)
        toolWindow.contentManager.addContent(content)
    }
}

class FileChangeEntryCellRenderer(private val project: Project) : JLabel(), ListCellRenderer<FileChangeEntry> {

    override fun getListCellRendererComponent(
        list: JList<out FileChangeEntry>,
        value: FileChangeEntry,
        index: Int,
        isSelected: Boolean,
        cellHasFocus: Boolean
    ): Component {
        val relativePath = value.virtualFile.path.removePrefix(project.basePath ?: "")

        text = when (value.type) {
            FileChangeEntry.ChangeType.CREATED -> "✔️ Created: $relativePath"
            FileChangeEntry.ChangeType.UPDATED -> "✏️ Updated: $relativePath"
        }

        icon = when (value.type) {
            FileChangeEntry.ChangeType.CREATED -> AllIcons.General.Add
            FileChangeEntry.ChangeType.UPDATED -> AllIcons.Actions.Edit
        }

        foreground = when (value.type) {
            FileChangeEntry.ChangeType.CREATED -> JBColor.GREEN
            FileChangeEntry.ChangeType.UPDATED -> JBColor.ORANGE
        }

        font = if (value.virtualFile.isDirectory) {
            font.deriveFont(Font.ITALIC)
        } else {
            font.deriveFont(Font.PLAIN)
        }

        if (isSelected) {
            background = list.selectionBackground
            foreground = list.selectionForeground
        } else {
            background = list.background
        }

        isEnabled = list.isEnabled
        setFont(list.font)
        isOpaque = true

        return this
    }
}
