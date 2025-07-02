
import org.jetbrains.changelog.Changelog
import org.jetbrains.changelog.Changelog.OutputType
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "1.9.22"
    id("org.jetbrains.intellij") version "1.17.0"
    id("org.jetbrains.changelog") version "2.2.0"
}

group = "com.yourcompany.geminiplugin"
version = "1.0.0"

repositories {
    mavenCentral()
}

// Configure the IntelliJ plugin settings
intellij {
    version.set("2023.3.6")
    type.set("IC") // Use "IU" for IntelliJ IDEA Ultimate

    plugins.set(listOf("org.jetbrains.kotlin"))
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

tasks {
    // Set the JVM target for Kotlin compilation
    withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "17"
    }

    patchPluginXml {
        sinceBuild.set("233") // Corresponds to 2023.3
        untilBuild.set("251.*")
    }

    runIde {
        // Allow running with an API key for testing
        environment("GEMINI_API_KEY", System.getenv("GEMINI_API_KEY"))
    }
}

changelog {
    version.set(project.version.toString())
    path.set(file("CHANGELOG.md").absolutePath)
    header.set(provider { "[${version.get()}]" })
    itemPrefix.set("-")
    
}
