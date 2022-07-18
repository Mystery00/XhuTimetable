// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    repositories {
        //如果拉取太慢，请注释掉该行
        maven("https://nexus3.mystery0.vip/repository/maven-public/")
        google()
        mavenCentral()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:7.2.1")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.6.21")

        // NOTE: Do not place your application dependencies here; they belong
        // in the individual module build.gradle.kts files
    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}