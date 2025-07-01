package com.yourcompany.geminiplugin

import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.yourcompany.geminiplugin.settings.GeminiSettingsState
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter

@Service
class GeminiCliService {

    fun execute(project: Project, prompt: String, context: String) {
        ProgressManager.getInstance().run(object : Task.Backgroundable(project, "Running Gemini AI", true) {
            override fun run(indicator: ProgressIndicator) {
                try {
                    val settings = GeminiSettingsState.getInstance()
                    val apiKey = settings.apiKey.ifBlank {
                        System.getenv("GEMINI_API_KEY")
                    }

                    if (apiKey.isNullOrBlank()) {
                        showError("Gemini API Key is not set. Please set it in the plugin settings or as an environment variable.")
                        return
                    }

                    // Construct the command to run gemini-cli
                    // The 'gemini-cli' executable is assumed to be in the system's PATH.
                    val command = listOf("gemini-cli", "--prompt", prompt)

                    val processBuilder = ProcessBuilder(command)
                    // Set the GEMINI_API_KEY environment variable for the process
                    processBuilder.environment()["GEMINI_API_KEY"] = apiKey
                    processBuilder.redirectErrorStream(true) // Redirect error stream to stdout for easier capture

                    val process = processBuilder.start()

                    // Write the context to the stdin of the gemini-cli process
                    // This is crucial for providing the relevant project context to the CLI.
                    val writer = OutputStreamWriter(process.outputStream)
                    writer.write(context)
                    writer.close() // Important: close the writer to signal end of input to the CLI

                    val reader = BufferedReader(InputStreamReader(process.inputStream))
                    val output = reader.readText() // Read all output from the CLI

                    val exitCode = process.waitFor() // Wait for the process to complete

                    ApplicationManager.getApplication().invokeLater {
                        if (exitCode == 0) {
                            // Display the raw textual output from the CLI to the user
                            Messages.showInfoMessage(project, output, "Gemini AI Output")
                            showSuccess("Gemini AI command executed successfully.")
                        } else {
                            // Display error message and CLI output if the command failed
                            showError("Gemini CLI failed with exit code $exitCode:\n$output")
                        }
                    }
                } catch (e: Exception) {
                    // Catch any exceptions during process execution or I/O
                    showError("An error occurred while running Gemini CLI: ${e.message}")
                }
            }
        })
    }

    private fun showSuccess(message: String) {
        Notifications.Bus.notify(Notification("GeminiAIGroup", "Gemini AI", message, NotificationType.INFORMATION))
    }

    private fun showError(message: String) {
        Notifications.Bus.notify(Notification("GeminiAIGroup", "Gemini AI Error", message, NotificationType.ERROR))
    }

    companion object {
        fun getInstance(): GeminiCliService = service()
    }
}