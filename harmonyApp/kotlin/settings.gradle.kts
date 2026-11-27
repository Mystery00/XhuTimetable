rootProject.name = "XhuTimetableHarmony"

pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        maven("https://maven.eazytec-cloud.com/nexus/repository/maven-public/")
    }
}

dependencyResolutionManagement {
    val localProperties = java.util.Properties().apply {
        val source = file("../../local.properties")
        if (source.isFile) source.inputStream().use { load(it) }
    }
    repositories {
        // 源码归档按 POM 解析，避免许可证收集时出现原生变体歧义。
        exclusiveContent {
            forRepository {
                maven {
                    name = "sourceArchiveMetadata"
                    url = uri("https://repo.maven.apache.org/maven2/")
                    metadataSources {
                        mavenPom()
                        artifact()
                        ignoreGradleMetadataRedirection()
                    }
                }
            }
            filter {
                includeModule("io.github.vinceglb", "filekit-core-iosarm64")
                includeModule("io.github.vinceglb", "filekit-coil-iosarm64")
                includeModule("io.github.vinceglb", "filekit-dialogs-iosarm64")
                includeModule("de.jensklingenberg.ktorfit", "ktorfit-lib-light-iosarm64")
                includeModule("de.jensklingenberg.ktorfit", "ktorfit-annotations-iosarm64")
                includeModule("org.jetbrains.compose.material", "material-icons-core-uikitarm64")
                includeModule("org.jetbrains.compose.material", "material-icons-extended-uikitarm64")
            }
        }
        maven("https://maven.pkg.github.com/Mystery00/sheets-compose-dialogs") {
            content { includeGroup("vip.mystery0.sheets-compose-dialogs") }
            credentials {
                username = localProperties.getProperty("GITHUB_USERNAME")
                    ?.takeIf { it.isNotBlank() } ?: System.getenv("GITHUB_USERNAME")
                password = localProperties.getProperty("GITHUB_PASSWORD")
                    ?.takeIf { it.isNotBlank() } ?: System.getenv("GITHUB_PASSWORD")
            }
        }
        maven("https://maven.eazytec-cloud.com/nexus/repository/maven-public/")
        if (providers.gradleProperty("harmonyArch").orElse("arm64").get() == "x64") {
            maven("https://maven.eazytec-cloud.com/nexus/repository/maven-labs/") {
                content {
                    includeGroup("io.insert-koin")
                    includeGroup("io.coil-kt.coil3")
                    includeGroup("co.touchlab")
                }
            }
            maven("https://maven.eazytec-cloud.com/nexus/repository/maven-inner/") {
                content {
                    includeGroup("app.cash.paging")
                    includeGroup("androidx.paging")
                    includeGroup("org.jetbrains.androidx.paging")
                    includeGroupAndSubgroups("org.kotlincrypto")
                }
            }
        }
        mavenCentral()
        google()
    }
}

include(":composeApp", ":portedLibraries", ":loggerChecks")
