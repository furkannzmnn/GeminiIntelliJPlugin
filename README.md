
# Gemini AI IntelliJ Plugin

This plugin integrates with the Gemini CLI to provide AI-powered code editing features within IntelliJ IDEA.

## Building the Plugin

1.  Open this project in IntelliJ IDEA.
2.  Configure the Gradle plugin if prompted.
3.  Set the `GEMINI_API_KEY` environment variable in your system or in the `runIde` task in `build.gradle.kts`.
4.  Run the `runIde` Gradle task to start a new instance of IntelliJ IDEA with the plugin installed.

## Usage

1.  Go to `Tools > Gemini Prompt` or right-click in the project view and select `Gemini Prompt`.
2.  Enter a natural language prompt in the dialog box.
3.  The plugin will call the Gemini CLI and display a diff of the suggested changes.
4.  Review and approve the changes to apply them to your project.
