import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.kotlinSerialize)
    alias(libs.plugins.kotlinKsp)
    alias(libs.plugins.aboutLibraries)
    alias(libs.plugins.ktorfit)
    alias(libs.plugins.room)
}

room {
    schemaDirectory("$projectDir/schemas")
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

val packageName = "vip.mystery0.xhu.timetable"
val gitVersionCode: Int = "git rev-list HEAD --count".runCommand().toInt()
val gitVersionName = "git rev-parse --short=8 HEAD".runCommand()
val appVersionName = "1.6.1"

kotlin {
    androidTarget {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_21)
        }
    }

    compilerOptions {
        optIn.add("kotlin.time.ExperimentalTime")
        optIn.add("androidx.compose.material3.ExperimentalMaterial3Api")
        optIn.add("dev.whyoleg.cryptography.DelicateCryptographyApi")

        freeCompilerArgs.add("-Xexpect-actual-classes")
    }

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
            linkerOpts.add("-lsqlite3")
        }
    }
    
    sourceSets {
        androidMain.dependencies {
            implementation(libs.androidx.core.ktx)
            implementation(libs.androidx.activity.compose)
            implementation(libs.androidx.appcompat)
            implementation(libs.androidx.splashscreen)
            implementation(libs.androidx.browser)
            implementation(libs.androidx.work.runtime.ktx)
            implementation(libs.androidx.glance)
            implementation(libs.androidx.glance.widget)
            implementation(libs.material)
            //ktor
            implementation(libs.ktor.client.okhttp)
            //koin
            implementation(libs.koin.android)
            //room
            implementation(libs.androidx.room.ktx)
            //mmkv
            implementation(libs.mmkv.android)
            //accompanist
            implementation(libs.accompanist.permissions)
            //apache-compress
            implementation(libs.apache.compress)
        }
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            //common-viewmodel
            implementation(libs.androidx.lifecycle.viewmodel)
            //common-navigation
            implementation(libs.androidx.navigation)
            //material-icons
            implementation(libs.material.icon)
            implementation(libs.material.icon.extended)
            //kotlinx-serialization
            implementation(libs.kotlinx.serialization)
            //ktorfit
            implementation(libs.ktorfit)
            //ktor
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.websockets)
            implementation(libs.ktor.content.negotiation)
            implementation(libs.ktor.serialization.json)
            //koin
            implementation(project.dependencies.platform(libs.koin.bom))
            implementation(libs.koin.compose)
            implementation(libs.koin.viewmodel)
            implementation(libs.koin.navigation)
            //coil
            implementation(project.dependencies.platform(libs.coil.bom))
            implementation(libs.coil.compose)
            implementation(libs.coil.ktor3)
            implementation(libs.coil.cache.control)
            //kermit
            implementation(libs.kermit)
            implementation(libs.kermit.koin)
            //cmptoast
            implementation(libs.cmptoast)
            //filekit
            implementation(libs.filekit.core)
            implementation(libs.filekit.coil)
            implementation(libs.filekit.dialogs.compose)
            //aboutlibraries
            implementation(libs.aboutlibraries.core)
            implementation(libs.aboutlibraries.compose.core)
            implementation(libs.aboutlibraries.compose.m3)
            //room
            implementation(libs.androidx.room)
            //preference
            implementation(libs.compose.preference)
            //kotlin-crypto-hash
            implementation(project.dependencies.platform(libs.kotlin.crypto.hash.bom))
            implementation(libs.kotlin.crypto.hash.md)
            implementation(libs.kotlin.crypto.hash.sha1)
            implementation(libs.kotlin.crypto.hash.sha2)
            //cryptography
            implementation(project.dependencies.platform(libs.cryptography.bom))
            implementation(libs.cryptography.core)
            implementation(libs.cryptography.provider.optimal)
            //paging
            implementation(libs.paging.common)
            implementation(libs.paging.compose)
            //sheets-compose-dialogs
            implementation(libs.sheets.compose.dialogs)
            implementation(libs.sheets.compose.dialogs.calendar)
            implementation(libs.sheets.compose.dialogs.color)
            implementation(libs.sheets.compose.dialogs.clock)
            implementation(libs.sheets.compose.dialogs.datetime)
            implementation(libs.sheets.compose.dialogs.info)
            implementation(libs.sheets.compose.dialogs.input)
            implementation(libs.sheets.compose.dialogs.list)
            implementation(libs.sheets.compose.dialogs.option)
            implementation(libs.sheets.compose.dialogs.state)
            //zoomimage
            implementation(libs.zoomimage)
            //krop
            implementation(libs.krop)
            implementation(libs.krop.filekit)
        }
        iosMain.dependencies {
            //ktor
            implementation(libs.ktor.client.darwin)
            implementation(libs.ios.settings)
        }
    }
}

android {
    namespace = packageName
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = packageName
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = gitVersionCode
        versionName = appVersionName

        setProperty("archivesBaseName", "XhuTimetable-${versionName}")
        ndk {
            abiFilters.add("armeabi-v7a")
            abiFilters.add("arm64-v8a")
        }
        manifestPlaceholders["JPUSH_APPKEY"] = ""
        manifestPlaceholders["JPUSH_CHANNEL"] = "developer-default"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "THIRD-PARTY.txt"
        }
    }
    signingConfigs {
        create("sign")
    }
    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            resValue(
                "string",
                "feature_api_key",
                "65041db9-520c-4962-a512-34fd055abeae/41eFdAIdx5mMavrd4UYjJtpaz4UJEQWvFMTTmVhJ"
            )
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
            val nightly = System.getenv("NIGHTLY")?.toBoolean() == true

            resValue(
                "string",
                "feature_api_key",
                "491cab74-338f-4cfa-8192-3d7f985ed8b5/41eFdAIdx5mMavrd4UYjJtpaz4UJEQWvFMTTmVhJ"
            )
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
            signingConfig = signingConfigs.getByName("sign")
            manifestPlaceholders["JPUSH_PKGNAME"] = packageName
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    buildFeatures {
        buildConfig = true
    }
    androidResources {
        generateLocaleConfig = true
    }
    externalNativeBuild {
        cmake {
            path = file("src/androidMain/jni/CMakeLists.txt")
        }
    }
    ndkVersion = "28.1.13356709"
}

dependencies {
    add("kspAndroid", libs.androidx.room.compiler)
    add("kspIosSimulatorArm64", libs.androidx.room.compiler)
    add("kspIosX64", libs.androidx.room.compiler)
    add("kspIosArm64", libs.androidx.room.compiler)
}

aboutLibraries {
    offlineMode = true
    collect {
        fetchRemoteLicense = false
        fetchRemoteFunding = false
    }
    android {
        registerAndroidTasks = false
    }
    export {
        outputFile = file("src/commonMain/composeResources/files/aboutlibraries.json")
    }
}

tasks.register("updateAppleBuildVersion") {
    doLast {
        val configTemplate = rootProject.file("iosApp/Configuration/Config.xcconfig.template")
        val config = rootProject.file("iosApp/Configuration/Config.xcconfig")
        val content = configTemplate.readText()
        val newContent = content
            .replace("{appVersionName}", appVersionName)
            .replace("{gitVersionCode}", gitVersionCode.toString())
        config.writeText(newContent)
        println("Updated Config.xcconfig with version $appVersionName (Build $gitVersionCode)")
    }
}

apply(from = rootProject.file("signing.gradle"))
