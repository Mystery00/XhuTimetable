plugins {
    id("com.android.application")
    id("kotlin-android")
    id("kotlin-kapt")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
    id("com.huawei.agconnect")
}

fun String.runCommand(workingDir: File = file("./")): String {
    val parts = this.split("\\s".toRegex())
    val proc = ProcessBuilder(*parts.toTypedArray())
        .directory(workingDir)
        .redirectOutput(ProcessBuilder.Redirect.PIPE)
        .redirectError(ProcessBuilder.Redirect.PIPE)
        .start()

    proc.waitFor(1, TimeUnit.MINUTES)
    return proc.inputStream.bufferedReader().readText().trim()
}

val gitVersionCode: Int = "git rev-list HEAD --count".runCommand().toInt()
val gitVersionName = "git rev-parse --short=8 HEAD".runCommand()
val packageName = "vip.mystery0.xhu.timetable"

android {
    compileSdk = 34
    buildToolsVersion = "34.0.0"

    defaultConfig {
        applicationId = packageName
        minSdk = 26
        targetSdk = 34
        versionCode = gitVersionCode
        versionName = "1.5.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
        setProperty("archivesBaseName", "XhuTimetable-${versionName}")
        ndk {
            moduleName = "bspatch"
            abiFilters.add("armeabi-v7a")
            abiFilters.add("arm64-v8a")
        }
        manifestPlaceholders["JPUSH_APPKEY"] = ""
        manifestPlaceholders["JPUSH_CHANNEL"] = "developer-default"
        externalNativeBuild {
            cmake {
                cppFlags += ""
            }
        }
        ksp {
            arg(RoomSchemaArgProvider(File(projectDir, "schemas")))
        }
        resourceConfigurations.add("en")
        resourceConfigurations.add("zh")
    }
    signingConfigs {
        create("release") {
            storeFile = file(SignConfig.signKeyStoreFile)
            storePassword = SignConfig.signKeyStorePassword
            keyAlias = SignConfig.signKeyAlias
            keyPassword = SignConfig.signKeyPassword
        }
    }
    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            resValue("string", "app_name", "西瓜课表-debug")
            resValue("string", "app_version_code", gitVersionCode.toString())
            resValue(
                "string",
                "app_version_name",
                "${defaultConfig.versionName}.d$gitVersionCode.$gitVersionName"
            )
            resValue("color", "ic_launcher_background", "#FFEB3B")
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            versionNameSuffix = ".d$gitVersionCode.$gitVersionName"
            manifestPlaceholders["JPUSH_PKGNAME"] = "${packageName}${applicationIdSuffix}"
        }
        release {
            val nightly = System.getenv("NIGHTLY")?.toBoolean() ?: false

            resValue("string", "app_name", "西瓜课表")
            resValue("string", "app_version_code", gitVersionCode.toString())
            if (nightly) {
                resValue(
                    "string",
                    "app_version_name",
                    "${defaultConfig.versionName}.n$gitVersionCode.nightly"
                )
                resValue("color", "ic_launcher_background", "#00BCD4")
                isMinifyEnabled = false
                proguardFiles(
                    getDefaultProguardFile("proguard-android-optimize.txt"),
                    "proguard-rules.pro"
                )
                versionNameSuffix = ".n$gitVersionCode.nightly"
            } else {
                resValue(
                    "string",
                    "app_version_name",
                    "${defaultConfig.versionName}.r$gitVersionCode.$gitVersionName"
                )
                isMinifyEnabled = true
                proguardFiles(
                    getDefaultProguardFile("proguard-android-optimize.txt"),
                    "proguard-rules.pro"
                )
                versionNameSuffix = ".r$gitVersionCode.$gitVersionName"
            }
            signingConfig = signingConfigs.getByName("release")
            manifestPlaceholders["JPUSH_PKGNAME"] = packageName
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.5"
    }
    packaging {
        resources {
            excludes.add("/META-INF/{AL2.0,LGPL2.1}")
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    externalNativeBuild {
        cmake {
            path = file("src/main/jni/CMakeLists.txt")
            version = "3.22.1"
        }
    }
    namespace = "vip.mystery0.xhu.timetable"
}

configurations {
    all {
        exclude(group = "com.huawei.hms", module = "hmscoreinstaller")
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.core:core-splashscreen:1.0.1")
    implementation("androidx.browser:browser:1.7.0")
    implementation("androidx.concurrent:concurrent-futures-ktx:1.1.0")
    implementation("androidx.glance:glance:1.0.0")
    implementation("androidx.glance:glance-appwidget:1.0.0")
    //compose
    implementation("androidx.compose:compose-bom:2024.01.00")
    implementation("androidx.compose.material3:material3:1.2.0-rc01")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material:material-icons-core")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.compose.material3:material3-window-size-class")
    implementation("androidx.activity:activity-compose")
    implementation("androidx.compose.animation:animation:1.6.0")
    //paging3
    implementation("androidx.paging:paging-runtime-ktx:3.2.1")
    implementation("androidx.paging:paging-compose:3.2.1")
    //accompanist
    val accompanistVersion = "0.34.0"
    implementation("com.google.accompanist:accompanist-pager-indicators:$accompanistVersion")
    implementation("com.google.accompanist:accompanist-permissions:$accompanistVersion")
    //sheets-compose-dialogs
    val sheetsComposeDialogsVersion = "1.2.1"
    implementation("com.maxkeppeler.sheets-compose-dialogs:core:$sheetsComposeDialogsVersion")
    implementation("com.maxkeppeler.sheets-compose-dialogs:calendar:$sheetsComposeDialogsVersion")
    implementation("com.maxkeppeler.sheets-compose-dialogs:color:$sheetsComposeDialogsVersion")
    implementation("com.maxkeppeler.sheets-compose-dialogs:clock:$sheetsComposeDialogsVersion")
    implementation("com.maxkeppeler.sheets-compose-dialogs:date-time:$sheetsComposeDialogsVersion")
    implementation("com.maxkeppeler.sheets-compose-dialogs:info:$sheetsComposeDialogsVersion")
    implementation("com.maxkeppeler.sheets-compose-dialogs:input:$sheetsComposeDialogsVersion")
    implementation("com.maxkeppeler.sheets-compose-dialogs:list:$sheetsComposeDialogsVersion")
    implementation("com.maxkeppeler.sheets-compose-dialogs:option:$sheetsComposeDialogsVersion")
    //lottie
    implementation("com.airbnb.android:lottie-compose:6.3.0")
    //room
    implementation("androidx.room:room-runtime:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    //work manager
    implementation("androidx.work:work-runtime-ktx:2.9.0")
    //koin
    implementation("io.insert-koin:koin-android:3.5.3")
    implementation("io.insert-koin:koin-androidx-workmanager:3.5.3")
    implementation("io.insert-koin:koin-androidx-compose:3.5.3")
    //coil
    val coilVersion = "2.5.0"
    implementation("io.coil-kt:coil-compose:$coilVersion")
    implementation("io.coil-kt:coil-gif:$coilVersion")
    //retrofit
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-moshi:2.9.0")
    //moshi
    implementation("com.squareup.moshi:moshi:1.15.1")
    implementation("com.squareup.moshi:moshi-kotlin:1.15.1")
    //mmkv
    implementation("com.tencent:mmkv-static:1.3.3")
    //preference
    implementation("me.zhanghai.compose.preference:library:1.0.0")
    //AppCenter
    val appCenterSdkVersion = "5.0.4"
    implementation("com.microsoft.appcenter:appcenter-analytics:${appCenterSdkVersion}")
    implementation("com.microsoft.appcenter:appcenter-crashes:${appCenterSdkVersion}")
    //ucrop
    implementation("com.github.yalantis:ucrop:2.2.8")
    //feature-probe
    implementation("com.featureprobe:client-sdk-android:2.0.2@aar")
    implementation("net.java.dev.jna:jna:5.14.0@aar")
    //jg-push
    implementation("cn.jiguang.sdk:jpush-google:5.2.2")
    implementation("cn.jiguang.sdk.plugin:huawei:5.2.2")
    implementation("com.huawei.hms:push:6.12.0.300")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    kotlinOptions.freeCompilerArgs += "-opt-in=kotlin.RequiresOptIn"
}