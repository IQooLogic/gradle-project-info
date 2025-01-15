plugins {
    id("java")
    id("org.jetbrains.intellij.platform") version "2.1.0"
    kotlin("jvm") version "2.0.21"
}

group = "rs.devlabs"
version = "1.0.4"

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    intellijPlatform {
        intellijIdeaCommunity("2024.1.7")
        bundledPlugins(
            "com.intellij.gradle",
            "Git4Idea"
        )
        pluginVerifier()
        instrumentationTools()
    }
}

intellijPlatform {
    instrumentCode = false
    buildSearchableOptions = false

    pluginConfiguration {
        ideaVersion {
            sinceBuild = "233"
            untilBuild = provider { null }
        }
        changeNotes = """
            <ul>
            <li>light icon has border now</li>
            <li>updated idea version compatibility ('Until Build' setting)</li>
            </ul>
        """.trimIndent()
    }

    pluginVerification {
        ides {
            recommended()
        }
    }

//    publishing {
//        val intellijPublishToken: String by project
//        token = intellijPublishToken
//    }
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}
