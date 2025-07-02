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
import com.intellij.openapi.vfs.LocalFileSystem
import com.yourcompany.geminiplugin.settings.GeminiSettingsState
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.io.File

@Service
class GeminiCliService {

    fun execute(project: Project, prompt: String, context: String, onOutput: (String) -> Unit) {
        ProgressManager.getInstance().run(object : Task.Backgroundable(project, "Running Gemini AI", true) {
            override fun run(indicator: ProgressIndicator) {
                val fullOutput = StringBuilder()
                try {
                    val settings = GeminiSettingsState.getInstance()
                    val apiKey = settings.apiKey.ifBlank {
                        System.getenv("GEMINI_API_KEY")
                    }

                    if (apiKey.isNullOrBlank()) {
                        showError("Gemini API Key is not set. Please set it in the plugin settings or as an environment variable.")
                        println("ERROR: Gemini API Key is not set.")
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
                    println("Executing command: ${command.joinToString(" ")}")

                    val processBuilder = ProcessBuilder(command)
                    val projectRoot = File(project.basePath ?: ".")
                    processBuilder.directory(projectRoot)
                    processBuilder.environment()["GEMINI_API_KEY"] = apiKey
                    processBuilder.redirectErrorStream(true)

                    val process = processBuilder.start()

                    val writer = OutputStreamWriter(process.outputStream)
                    writer.write(context)
                    writer.close()
                    println("Context sent to stdin.\n")

                    val reader = BufferedReader(InputStreamReader(process.inputStream))
                    var line: String?

                    while (reader.readLine().also { line = it } != null) {
                        println("CLI Output Line: $line") // Log every line
                        fullOutput.append(line).append("\n")
                    }

                    val exitCode = process.waitFor()
                    println("CLI process exited with code: $exitCode") // Log exit code

                    ApplicationManager.getApplication().invokeLater {
                        onOutput(fullOutput.toString())

                        if (exitCode == 0) {
                            showSuccess("Gemini AI command executed successfully.")
                        } else {
                            showError("Gemini CLI failed with exit code $exitCode.")
                            println("ERROR: Gemini CLI failed with exit code $exitCode. Full output:\n$fullOutput") // Log error with full output
                        }
                    }
                } catch (e: Exception) {
                    showError("An error occurred while running Gemini CLI: ${e.message}")
                    println("EXCEPTION: An error occurred while running Gemini CLI: ${e.message}") // Log exception
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