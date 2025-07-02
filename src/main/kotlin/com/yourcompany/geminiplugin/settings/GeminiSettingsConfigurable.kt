
package com.yourcompany.geminiplugin.settings

import com.intellij.openapi.options.Configurable
import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.FormBuilder
import javax.swing.JComponent
import javax.swing.JPanel

class GeminiSettingsConfigurable : Configurable {
    private val apiKeyField = JBTextField()
    private val geminiExecutablePathField = JBTextField()
    private val nodeExecutablePathField = JBTextField()
    private val settings = GeminiSettingsState.getInstance()

    override fun getDisplayName(): String {
        return "Gemini AI Assistant"
    }

    override fun createComponent(): JComponent {
        return FormBuilder.createFormBuilder()
            .addLabeledComponent("Gemini API Key:", apiKeyField)
            .addLabeledComponent("Path to Gemini Executable:", geminiExecutablePathField)
            .addLabeledComponent("Path to Node.js Executable:", nodeExecutablePathField)
            .panel
    }

    override fun isModified(): Boolean {
        return apiKeyField.text != settings.apiKey ||
               geminiExecutablePathField.text != settings.geminiExecutablePath ||
               nodeExecutablePathField.text != settings.nodeExecutablePath
    }

    override fun apply() {
        settings.apiKey = apiKeyField.text
        settings.geminiExecutablePath = geminiExecutablePathField.text
        settings.nodeExecutablePath = nodeExecutablePathField.text
    }

    override fun reset() {
        apiKeyField.text = settings.apiKey
        geminiExecutablePathField.text = settings.geminiExecutablePath
        nodeExecutablePathField.text = settings.nodeExecutablePath
    }
}
