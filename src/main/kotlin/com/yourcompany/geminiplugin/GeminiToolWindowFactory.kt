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
import javax.swing.JPanel
import javax.swing.JButton
import java.awt.BorderLayout
import java.awt.FlowLayout

class GeminiToolWindowFactory : ToolWindowFactory {
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

        // Output Panel
        val outputPanel = JPanel(BorderLayout())
        val responseLabel = JBLabel("Gemini AI Response:")
        val responseTextArea = JBTextArea(15, 50).apply { isEditable = false }
        val responseScrollPane = JBScrollPane(responseTextArea)
        outputPanel.add(responseLabel, BorderLayout.NORTH)
        outputPanel.add(responseScrollPane, BorderLayout.CENTER)

        mainPanel.add(outputPanel, BorderLayout.CENTER)

        // Add padding to the main panel
        mainPanel.border = JBUI.Borders.empty(10)

        submitButton.addActionListener {
            val prompt = promptTextArea.text
            if (prompt.isNotBlank()) {
                responseTextArea.text = ""
                val editor = FileEditorManager.getInstance(project).selectedTextEditor
                val currentFile: VirtualFile? = editor?.virtualFile
                val context = currentFile?.path ?: ""

                GeminiCliService.getInstance().execute(project, prompt, context) {
                    output -> responseTextArea.append(output + "\n")
                }
            }
        }

        val content = ContentFactory.getInstance().createContent(mainPanel, "Gemini AI", false)
        toolWindow.contentManager.addContent(content)
    }
}