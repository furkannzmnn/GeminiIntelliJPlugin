package com.yourcompany.geminiplugin

import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import com.yourcompany.geminiplugin.settings.GeminiSettingsState
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import kotlin.text.RegexOption

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
                        "--sandbox", "false",
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
                        // Always display the full raw output in the tool window
                        onOutput(output)

                        if (exitCode == 0) {
                            val fileEdits = parseGeminiOutput(output)
                            if (fileEdits.isNotEmpty()) {
                                WriteCommandAction.runWriteCommandAction(project) {
                                    for (edit in fileEdits) {
                                        val file = LocalFileSystem.getInstance().refreshAndFindFileByPath(edit.filePath)
                                        if (file != null) {
                                            // Overwrite existing file
                                            file.setBinaryContent(edit.newContent.toByteArray())
                                            showSuccess("Updated file: ${edit.filePath}")
                                        } else {
                                            // Create new file
                                            val parentPath = edit.filePath.substringBeforeLast('/')
                                            val fileName = edit.filePath.substringAfterLast('/')
                                            val parentDir =
                                                LocalFileSystem.getInstance().refreshAndFindFileByPath(parentPath)
                                            if (parentDir != null && parentDir.isDirectory) {
                                                val newFile = parentDir.createChildData(this, fileName)
                                                newFile.setBinaryContent(edit.newContent.toByteArray())
                                                showSuccess("Created new file: ${edit.filePath}")
                                            } else {
                                                showError("Could not create file: ${edit.filePath}. Parent directory not found or is not a directory.")
                                            }
                                        }
                                    }
                                }
                                showSuccess("Gemini AI has applied file changes.")
                            } else {
                                showSuccess("Gemini AI command executed successfully. No file changes detected in output.")
                            }
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

    data class FileEdit(val filePath: String, val newContent: String)

    fun parseGeminiOutput(output: String): List<FileEdit> {
        val fileEdits = mutableListOf<FileEdit>()

        // This regex captures:
        // 1. The file path between "in ..." and " would look:"
        // 2. The code block between triple backticks ```
        val regex = Regex(
            """refactored code in (.*?) would look:\s+```(?:\w+)?\s+(.*?)```""",
            setOf(RegexOption.DOT_MATCHES_ALL, RegexOption.IGNORE_CASE)
        )

        regex.findAll(output).forEach { match ->
            val filePath = match.groups[1]?.value?.trim() ?: return@forEach
            val code = match.groups[2]?.value?.trim() ?: return@forEach

            if (filePath.isNotEmpty() && code.isNotEmpty()) {
                fileEdits.add(FileEdit(filePath, code))
            }
        }

        return fileEdits
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
