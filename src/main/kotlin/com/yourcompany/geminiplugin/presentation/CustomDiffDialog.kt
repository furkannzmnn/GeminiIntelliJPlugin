package com.yourcompany.geminiplugin.presentation

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.JBColor
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.components.JBTextArea
import com.intellij.util.ui.JBUI
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Dimension
import java.awt.Font
import javax.swing.*

/**
 * Clean Architecture presentation layer dialog for showing file differences
 */
class CustomDiffDialog(
    private val project: Project,
    private val file: VirtualFile,
    private val oldContent: String?
) : JDialog() {

    init {
        setupDialog()
        createUI()
    }

    private fun setupDialog() {
        title = "File Changes: ${file.name}"
        isModal = true
        defaultCloseOperation = DISPOSE_ON_CLOSE
        preferredSize = Dimension(1200, 800)
        setLocationRelativeTo(null)
    }

    private fun createUI() {
        layout = BorderLayout()
        
        // Header
        val headerPanel = createHeaderPanel()
        add(headerPanel, BorderLayout.NORTH)
        
        // Content
        val contentPanel = createContentPanel()
        add(contentPanel, BorderLayout.CENTER)
        
        // Buttons
        val buttonPanel = createButtonPanel()
        add(buttonPanel, BorderLayout.SOUTH)
    }

    private fun createHeaderPanel(): JPanel {
        val panel = JPanel(BorderLayout())
        panel.border = JBUI.Borders.empty(10)
        
        val titleLabel = JLabel("File Changes: ${file.name}")
        titleLabel.font = titleLabel.font.deriveFont(Font.BOLD, 16f)
        panel.add(titleLabel, BorderLayout.CENTER)
        
        return panel
    }

    private fun createContentPanel(): JPanel {
        val panel = JPanel(BorderLayout())
        panel.border = JBUI.Borders.empty(10)
        
        val splitPane = JSplitPane(JSplitPane.HORIZONTAL_SPLIT)
        splitPane.dividerLocation = 600
        
        // Left panel - Original content
        val leftPanel = createTextPanel("Original Content", oldContent ?: "No previous content available", Color(255, 240, 240))
        splitPane.leftComponent = leftPanel
        
        // Right panel - New content
        val newContent = getCurrentFileContent()
        val rightPanel = createTextPanel("New Content", newContent, Color(240, 255, 240))
        splitPane.rightComponent = rightPanel
        
        panel.add(splitPane, BorderLayout.CENTER)
        return panel
    }

    private fun createTextPanel(title: String, content: String, backgroundColor: Color): JPanel {
        val panel = JPanel(BorderLayout())
        panel.border = JBUI.Borders.compound(
            JBUI.Borders.customLine(JBColor.GRAY, 1),
            JBUI.Borders.empty(5)
        )
        
        val titleLabel = JLabel(title)
        titleLabel.font = titleLabel.font.deriveFont(Font.BOLD, 14f)
        titleLabel.border = JBUI.Borders.empty(0, 0, 5, 0)
        panel.add(titleLabel, BorderLayout.NORTH)
        
        val textArea = JBTextArea(content)
        textArea.isEditable = false
        textArea.font = Font(Font.MONOSPACED, Font.PLAIN, 12)
        textArea.background = backgroundColor
        textArea.lineWrap = true
        textArea.wrapStyleWord = true
        
        val scrollPane = JBScrollPane(textArea)
        panel.add(scrollPane, BorderLayout.CENTER)
        
        return panel
    }

    private fun createButtonPanel(): JPanel {
        val panel = JPanel()
        panel.border = JBUI.Borders.empty(10)
        
        val applyButton = JButton("Apply Changes")
        applyButton.addActionListener { 
            applyChanges()
            dispose()
        }
        
        val ignoreButton = JButton("Ignore")
        ignoreButton.addActionListener { dispose() }
        
        panel.add(applyButton)
        panel.add(ignoreButton)
        
        return panel
    }

    private fun getCurrentFileContent(): String {
        return try {
            val document = com.intellij.openapi.fileEditor.FileDocumentManager.getInstance().getDocument(file)
            document?.text ?: "Unable to read file content"
        } catch (e: Exception) {
            "Error reading file: ${e.message}"
        }
    }

    private fun applyChanges() {
        // Changes are already applied since we're showing the current state
        // This method can be extended for additional actions if needed
    }
} 