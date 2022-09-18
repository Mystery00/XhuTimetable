plugins {
    id("com.android.application")
    id("kotlin-android")
    id("kotlin-kapt")
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

android {
    compileSdk = 33
    buildToolsVersion = "33.0.0"

    defaultConfig {
        applicationId = "vip.mystery0.xhu.timetable"
        minSdk = 26
        targetSdk = 33
        versionCode = gitVersionCode
        versionName = "1.3.3-beta1"

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
        externalNativeBuild {
            cmake {
                cppFlags += ""
            }
        }
        javaCompileOptions {
            annotationProcessorOptions {
                arguments += mapOf(
                    "room.schemaLocation" to "$projectDir/schemas",
                    "room.incremental" to "true",
                    "room.expandProjection" to "true"
                )
            }
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
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            versionNameSuffix = ".d$gitVersionCode.$gitVersionName"
        }
        release {
            resValue("string", "app_name", "西瓜课表")
            resValue("string", "app_version_code", gitVersionCode.toString())
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
            signingConfig = signingConfigs.getByName("release")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.2.0-rc02"
    }
    packagingOptions {
        resources {
            excludes.add("/META-INF/{AL2.0,LGPL2.1}")
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    externalNativeBuild {
        cmake {
            path = file("src/main/jni/CMakeLists.txt")
            version = "3.18.1"
        }
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.9.0")
    implementation("androidx.appcompat:appcompat:1.5.1")
    implementation("com.google.android.material:material:1.6.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.5.1")
    implementation("androidx.activity:activity-compose:1.5.1")
    implementation("androidx.core:core-splashscreen:1.0.0")
    implementation("androidx.browser:browser:1.4.0")
    implementation("androidx.concurrent:concurrent-futures-ktx:1.1.0")
    implementation("androidx.glance:glance-appwidget:1.0.0-alpha03")
    //compose
    val composeVersion = "1.2.0-rc03"
    implementation("androidx.compose.ui:ui:$composeVersion")
    implementation("androidx.compose.material:material:$composeVersion")
    implementation("androidx.glance:glance:1.0.0-alpha04")
    debugImplementation("androidx.compose.ui:ui-tooling:$composeVersion")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:$composeVersion")
    //accompanist
    val accompanistVersion = "0.25.0"
    implementation("com.google.accompanist:accompanist-flowlayout:$accompanistVersion")
    implementation("com.google.accompanist:accompanist-insets:$accompanistVersion")
    implementation("com.google.accompanist:accompanist-pager:accompanistVersion")
    implementation("com.google.accompanist:accompanist-pager-indicators:$accompanistVersion")
    implementation("com.google.accompanist:accompanist-swiperefresh:$accompanistVersion")
    implementation("com.google.accompanist:accompanist-placeholder-material:$accompanistVersion")
    implementation("com.google.accompanist:accompanist-systemuicontroller:$accompanistVersion")
    implementation("com.google.accompanist:accompanist-permissions:$accompanistVersion")
    //compose-material-dialogs
    val composeMaterialDialogsVersion = "0.8.1-rc"
    implementation("io.github.vanpra.compose-material-dialogs:core:$composeMaterialDialogsVersion")
    implementation("io.github.vanpra.compose-material-dialogs:datetime:$composeMaterialDialogsVersion")
    implementation("io.github.vanpra.compose-material-dialogs:color:$composeMaterialDialogsVersion")
    //lottie
    implementation("com.airbnb.android:lottie-compose:5.2.0")
    //room
    val roomVersion = "2.4.3"
    implementation("androidx.room:room-runtime:$roomVersion")
    kapt("androidx.room:room-compiler:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")
    //work manager
    implementation("androidx.work:work-runtime-ktx:2.7.1")
    //koin
    val koinVersion = "3.2.0"
    implementation("io.insert-koin:koin-android:$koinVersion")
    implementation("io.insert-koin:koin-androidx-workmanager:$koinVersion")
    implementation("io.insert-koin:koin-androidx-compose:$koinVersion")
    //coil
    val coilVersion = "2.2.1"
    implementation("io.coil-kt:coil-compose:$coilVersion")
    implementation("io.coil-kt:coil-gif:$coilVersion")
    //retrofit
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-moshi:2.9.0")
    //moshi
    implementation("com.squareup.moshi:moshi:1.14.0")
    implementation("com.squareup.moshi:moshi-kotlin:1.14.0")
    //mmkv
    implementation("com.tencent:mmkv-static:1.2.14")
    //eventbus
    implementation("org.greenrobot:eventbus:3.3.1")
    //preference
    implementation("com.github.alorma:compose-settings-ui:0.11.0")
    //AppCenter
    val appCenterSdkVersion = "4.4.5"
    implementation("com.microsoft.appcenter:appcenter-analytics:${appCenterSdkVersion}")
    implementation("com.microsoft.appcenter:appcenter-crashes:${appCenterSdkVersion}")
    //zloading，通过jitpack导入修改版本
//    implementation("com.zyao89:zloading:1.2.0")
    implementation("com.github.Mystery00:ZLoading:1.2.1")
    //ucrop
    implementation("com.github.yalantis:ucrop:2.2.8")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    kotlinOptions.freeCompilerArgs += "-opt-in=kotlin.RequiresOptIn"
}