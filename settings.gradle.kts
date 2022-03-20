dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        maven("https://nexus3.mystery0.vip/repository/maven-public/")
        maven("https://jitpack.io")
        google()
        mavenCentral()
    }
}
rootProject.name = "XhuTimetable"
include(":app")
