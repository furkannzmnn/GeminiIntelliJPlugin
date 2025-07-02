
package com.yourcompany.geminiplugin

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.components.JBTextArea
import javax.swing.JComponent
import javax.swing.JPanel
import java.awt.BorderLayout

class PromptDialog(private val project: Project, private val context: String) : DialogWrapper(true) {
    private val textArea = JBTextArea(10, 50)

    init {
        init()
        title = "Gemini AI Prompt"
    }

    override fun createCenterPanel(): JComponent {
        val panel = JPanel(BorderLayout())
        panel.add(textArea, BorderLayout.CENTER)
        return panel
    }

    override fun doOKAction() {
        val prompt = textArea.text
        if (prompt.isNotBlank()) {
            GeminiCliService.getInstance().execute(project, prompt, context,
                onOutput = { output -> println("Gemini CLI Output: $output") },
            )
        }
        super.doOKAction()
    }
}
