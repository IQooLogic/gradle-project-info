import org.jetbrains.intellij.platform.gradle.extensions.intellijPlatform

rootProject.name = "gradle-project-info"

pluginManagement {
    plugins {
        id("org.jetbrains.kotlin.jvm") version "2.4.0"
        id("org.jetbrains.changelog") version "2.5.0"
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
    id("org.jetbrains.intellij.platform.settings") version "2.17.0"
}

@Suppress("UnstableApiUsage")
dependencyResolutionManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()

        intellijPlatform {
            defaultRepositories()
        }
    }
}