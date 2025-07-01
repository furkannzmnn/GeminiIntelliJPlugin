
package com.yourcompany.geminiplugin.settings

import com.intellij.openapi.options.Configurable
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JTextField
import java.awt.BorderLayout

class GeminiSettingsConfigurable : Configurable {
    private val apiKeyField = JTextField()
    private val settings = GeminiSettingsState.getInstance()

    override fun getDisplayName(): String {
        return "Gemini AI Assistant"
    }

    override fun createComponent(): JComponent {
        val panel = JPanel(BorderLayout())
        panel.add(JLabel("Gemini API Key:"), BorderLayout.WEST)
        panel.add(apiKeyField, BorderLayout.CENTER)
        return panel
    }

    override fun isModified(): Boolean {
        return apiKeyField.text != settings.apiKey
    }

    override fun apply() {
        settings.apiKey = apiKeyField.text
    }

    override fun reset() {
        apiKeyField.text = settings.apiKey
    }
}
