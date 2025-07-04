package com.yourcompany.geminiplugin

import com.intellij.icons.AllIcons
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.OpenFileDescriptor
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.openapi.vfs.newvfs.BulkFileListener
import com.intellij.openapi.vfs.newvfs.events.VFileEvent
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.JBColor
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBList
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.components.JBTextArea
import com.intellij.ui.content.ContentFactory
import com.intellij.util.ui.JBUI
import com.yourcompany.geminiplugin.application.FileChangeEventHandler
import com.yourcompany.geminiplugin.application.FileChangePresenter
import com.yourcompany.geminiplugin.application.FileChangeUseCase
import com.yourcompany.geminiplugin.domain.FileChangeService
import com.yourcompany.geminiplugin.infrastructure.FileChangeRepositoryImpl
import com.yourcompany.geminiplugin.presentation.FileChangePresenterImpl
import kotlinx.coroutines.*
import java.awt.BorderLayout
import java.awt.Component
import java.awt.FlowLayout
import java.awt.Font
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.*

class GeminiToolWindowFactory : ToolWindowFactory {

    private val rawOutputTextArea = JBTextArea(10, 50).apply { isEditable = false }
    private val streamedOutputListModel = DefaultListModel<FileChangeEntry>()
    
    // Clean Architecture dependencies
    private lateinit var fileChangeRepository: FileChangeRepositoryImpl
    private lateinit var fileChangeService: FileChangeService
    private lateinit var fileChangePresenter: FileChangePresenter
    private lateinit var fileChangeUseCase: FileChangeUseCase
    private lateinit var fileChangeEventHandler: FileChangeEventHandler

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        initializeCleanArchitecture(project)
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

        // Register Clean Architecture file listener
        val fileListener = object : BulkFileListener {
            override fun before(events: MutableList<out VFileEvent>) {
                fileChangeEventHandler.handleBeforeEvent(events)
            }
            
            override fun after(events: MutableList<out VFileEvent>) {
                fileChangeEventHandler.handleAfterEvent(events)
            }
        }
        project.messageBus.connect(toolWindow.disposable).subscribe(VirtualFileManager.VFS_CHANGES, fileListener)

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
                handlePromptSubmission(prompt, project)
            }
        }

        val content = ContentFactory.getInstance().createContent(mainPanel, "Gemini AI", false)
        toolWindow.contentManager.addContent(content)
    }
    
    private fun initializeCleanArchitecture(project: Project) {
        fileChangeRepository = FileChangeRepositoryImpl(project)
        fileChangeService = FileChangeService(fileChangeRepository, project)
        fileChangePresenter = FileChangePresenterImpl(project, fileChangeService, streamedOutputListModel)
        fileChangeUseCase = FileChangeUseCase(fileChangeService, fileChangePresenter)
        fileChangeEventHandler = FileChangeEventHandler(fileChangeUseCase)
    }
    
    private fun handlePromptSubmission(prompt: String, project: Project) {
        rawOutputTextArea.text = ""
        streamedOutputListModel.clear()
        fileChangeUseCase.startTrackingFileChanges()

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