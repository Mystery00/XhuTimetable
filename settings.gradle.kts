rootProject.name = "XhuTimetable"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    resolutionStrategy {
        eachPlugin {
            if (requested.id.id == "com.huawei.agconnect") {
                useModule("com.huawei.agconnect:agcp:${requested.version}")
            }
        }
    }
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
        maven {
            url = uri("https://developer.huawei.com/repo/")
            content {
                includeGroupAndSubgroups("com.huawei")
            }
        }
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        maven {
            url = uri("https://maven.pkg.github.com/Mystery00/sheets-compose-dialogs")
            content {
                includeGroup("vip.mystery0.sheets-compose-dialogs")
            }
            credentials {
                setUsername(System.getenv("GITHUB_USERNAME"))
                setPassword(System.getenv("GITHUB_PASSWORD"))
            }
        }
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        maven {
            url = uri("https://developer.huawei.com/repo/")
            content {
                includeGroupAndSubgroups("com.huawei")
            }
        }
        mavenCentral()
    }
}

include(":composeApp")
