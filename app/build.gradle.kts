plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kotlin.ksp)
    alias(libs.plugins.about.libraries)
    alias(libs.plugins.huawei.ag.connect)
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
    namespace = packageName
    compileSdk = 35

    defaultConfig {
        applicationId = packageName
        minSdk = 26
        targetSdk = 35
        versionCode = gitVersionCode
        versionName = "1.5.6"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
        setProperty("archivesBaseName", "XhuTimetable-${versionName}")
        ndk {
            abiFilters.add("armeabi-v7a")
            abiFilters.add("arm64-v8a")
        }
        manifestPlaceholders["JPUSH_APPKEY"] = ""
        manifestPlaceholders["JPUSH_CHANNEL"] = "developer-default"
        ksp {
            arg(RoomSchemaArgProvider(File(projectDir, "schemas")))
        }
        androidResources.localeFilters.clear()
        androidResources.localeFilters.add("en")
        androidResources.localeFilters.add("zh")
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
        buildConfig = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "THIRD-PARTY.txt"
        }
    }
    externalNativeBuild {
        cmake {
            path = file("src/main/jni/CMakeLists.txt")
        }
    }
}

configurations.all {
    exclude(group = "com.huawei.hms", module = "hmscoreinstaller")
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.material)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material3.window.size)
    implementation(libs.androidx.material.icon)
    implementation(libs.androidx.material.icon.extended)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.animation)
    implementation(libs.androidx.splashscreen)
    implementation(libs.androidx.browser)
    implementation(libs.androidx.concurrent.futures)
    implementation(libs.androidx.glance)
    implementation(libs.androidx.glance.widget)
    //paging3
    implementation(libs.androidx.paging3)
    implementation(libs.androidx.paging3.compose)
    //accompanist
    implementation(libs.accompanist.permissions)
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
    //lottie
    implementation(libs.lottie)
    //room
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.room)
    implementation(libs.androidx.room.ktx)
    //work manager
    implementation(libs.androidx.workmanager)
    //koin
    implementation(libs.koin)
    implementation(libs.koin.compose)
    implementation(libs.koin.workmanager)
    //coil
    implementation(libs.coil.compose)
    implementation(libs.coil.gif)
    //retrofit
    implementation(libs.retrofit)
    implementation(libs.retrofit.moshi)
    //moshi
    implementation(libs.moshi)
    implementation(libs.moshi.kotlin)
    //mmkv
    implementation(libs.mmkv)
    //preference
    implementation(libs.compose.preference)
    //ucrop
    implementation(libs.ucrop)
    //feature-hub
    implementation(libs.feature.hub)
    implementation(libs.jackson)
    implementation(libs.jackson.datatype.jsr310)
    //jg-push
    implementation(libs.jpush.google)
    implementation(libs.jpush.plugin.huawei)
    implementation(libs.hms.push)
    //AboutLibraries
    implementation(libs.aboutlibraries)
    implementation(libs.aboutlibraries.compose)
    //zoomimage
    implementation(libs.zoomimage)
}

aboutLibraries {
    offlineMode = true
    fetchRemoteLicense = false
    fetchRemoteFunding = false
}