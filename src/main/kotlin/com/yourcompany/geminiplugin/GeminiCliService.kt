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
import com.yourcompany.geminiplugin.settings.GeminiSettingsState
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.io.OutputStreamWriter

@Service
class GeminiCliService {

    fun execute(project: Project, prompt: String, context: String, onOutput: (String) -> Unit) {
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

                    val command = listOf(
                        "gemini",
                        "--all_files",
                        "--model", "gemini-2.5-flash",
                        "--sandbox", "false",
                        "--yolo",
                        "--prompt", prompt
                    )
                    val processBuilder = ProcessBuilder(command)
                    val projectRoot = File(project.basePath ?: ".")
                    processBuilder.directory(projectRoot)
                    processBuilder.environment()["GEMINI_API_KEY"] = apiKey
                    processBuilder.redirectErrorStream(true)

                    val process = processBuilder.start()

                    val writer = OutputStreamWriter(process.outputStream)
                    writer.write(context)
                    writer.close()

                    val reader = BufferedReader(InputStreamReader(process.inputStream))
                    val output = reader.readText()

                    val exitCode = process.waitFor()

                    ApplicationManager.getApplication().invokeLater {
                        // Display the raw output in the tool window
                        onOutput(output)

                        if (exitCode == 0) {
                            showSuccess("Gemini CLI command executed successfully.")
                        } else {
                            showError("Gemini CLI failed with exit code $exitCode.")
                        }
                    }
                } catch (e: Exception) {
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
