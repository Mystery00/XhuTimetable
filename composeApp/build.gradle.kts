import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.kotlinSerialize)
    alias(libs.plugins.kotlinKsp)
    alias(libs.plugins.ktorfit)
    alias(libs.plugins.room)
    alias(libs.plugins.aboutLibraries)
}

room {
    schemaDirectory("$projectDir/schemas")
}

val gitVersionCode: Int = providers.exec {
    commandLine(
        "git",
        "rev-list",
        "HEAD",
        "--count"
    )
}.standardOutput.asText.get().trim().toInt()
val appVersionName = libs.versions.app.version.get()

kotlin {
    android {
        namespace = "vip.mystery0.xhu.timetable.shared"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_21)
        }
        androidResources {
            enable = true
        }
        withHostTest {}
    }

    compilerOptions {
        optIn.add("kotlin.time.ExperimentalTime")
        optIn.add("androidx.compose.material3.ExperimentalMaterial3Api")
        optIn.add("androidx.compose.material3.ExperimentalMaterial3ExpressiveApi")
        optIn.add("dev.whyoleg.cryptography.DelicateCryptographyApi")

        freeCompilerArgs.add("-Xexpect-actual-classes")
    }

    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }
    
    sourceSets {
        androidMain {
            kotlin.srcDir("src/androidIosMain/kotlin")
            kotlin.srcDir("src/platformMain/kotlin")
        }
        iosMain {
            kotlin.srcDir("src/androidIosMain/kotlin")
            kotlin.srcDir("src/platformMain/kotlin")
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
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
            //jpush
            implementation(libs.androidx.annotation)
            implementation(libs.gson)
            implementation(libs.jpush)
            implementation(libs.jpush.plugin.huawei)
            implementation(libs.jpush.plugin.huawei.hms)
            implementation(libs.jpush.plugin.oppo)
            implementation(libs.jpush.plugin.vivo)
        }
        commonMain.dependencies {
            implementation(libs.runtime)
            implementation(libs.foundation)
            implementation(libs.ui)
            implementation(libs.components.resources)
            implementation(libs.material3)
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
            //sqlite
            implementation(libs.androidx.sqlite.bundled)
            implementation(libs.ios.settings)
        }
    }
}

dependencies {
    add("kspAndroid", libs.androidx.room.compiler)
    add("kspIosSimulatorArm64", libs.androidx.room.compiler)
    add("kspIosArm64", libs.androidx.room.compiler)
}

aboutLibraries {
    offlineMode = true
    collect {
        fetchRemoteLicense = false
        fetchRemoteFunding = false
    }
    export {
        outputFile = file("src/commonMain/composeResources/files/aboutlibraries.json")
    }
}

// 将跨语言测试样本纳入缓存输出，CI 即使命中测试缓存也能读取样本。
tasks.matching { it.name == "testAndroidHostTest" }.configureEach {
    outputs.file(layout.buildDirectory.file("widget-tests/snapshot.json"))
}

// 同次构建请求导出许可证时，先生成清单再复制资源；普通构建仍可复用已有清单。
tasks.matching { it.name == "copyNonXmlValueResourcesForCommonMain" }.configureEach {
    mustRunAfter(tasks.named("exportLibraryDefinitions"))
}

tasks.register("updateAppleBuildVersion") {
    doLast {
        val configTemplate = rootProject.file("iosApp/Configuration/Config.xcconfig.template")
        val config = rootProject.file("iosApp/Configuration/Config.xcconfig")
        val versionConfig = rootProject.file("iosApp/Configuration/Version.xcconfig")
        val content = configTemplate.readText()
        val newContent = content
            .replace("{appVersionName}", appVersionName)
            .replace("{gitVersionCode}", gitVersionCode.toString())
            .replace("{debugPushAppKey}", libs.versions.debugPushAppKey.get())
            .replace("{releasePushAppKey}", libs.versions.releasePushAppKey.get())
            .replace("{pushChannel}", libs.versions.pushChannel.get())
        config.writeText(newContent)
        versionConfig.writeText(
            "CURRENT_PROJECT_VERSION=$gitVersionCode\nMARKETING_VERSION=$appVersionName\n"
        )
        println("Updated Config.xcconfig with version $appVersionName (Build $gitVersionCode)")
    }
}
