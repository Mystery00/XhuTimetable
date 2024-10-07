// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:8.7.0")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.23")
        classpath("com.huawei.agconnect:agcp:1.9.1.301")
    }
}

plugins {
    id("com.google.devtools.ksp") version "1.9.23-1.0.20" apply false
    id("com.mikepenz.aboutlibraries.plugin") version "11.1.4"
}

tasks.register("clean", Delete::class) {
    delete(rootProject.layout.buildDirectory)
}