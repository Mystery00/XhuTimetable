plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.kotlinSerialization)
}

val sheetModules = listOf("core", "calendar", "clock", "color", "date-time", "info", "input", "list", "option", "state")
val sourceCoordinates = buildMap {
    sheetModules.forEach { module ->
        put("sheets-$module", "vip.mystery0.sheets-compose-dialogs:$module-iosarm64:${libs.versions.sheets.get()}")
    }
    put("filekit", "io.github.vinceglb:filekit-core-iosarm64:${libs.versions.filekit.get()}")
    put("filekit-coil", "io.github.vinceglb:filekit-coil-iosarm64:${libs.versions.filekit.get()}")
    put("filekit-dialogs", "io.github.vinceglb:filekit-dialogs-iosarm64:${libs.versions.filekit.get()}")
    put("ktorfit", "de.jensklingenberg.ktorfit:ktorfit-lib-light-iosarm64:${libs.versions.ktorfit.get()}")
    put("ktorfit-annotations", "de.jensklingenberg.ktorfit:ktorfit-annotations-iosarm64:${libs.versions.ktorfit.get()}")
    put("preference", "me.zhanghai.compose.preference:preference-iosarm64:${libs.versions.preference.get()}")
    put("toast", "network.chaintech:cmptoast-iosarm64:${libs.versions.toast.get()}")
    put("icons-core", "org.jetbrains.compose.material:material-icons-core-uikitarm64:${libs.versions.icons.get()}")
    put("icons-extended", "org.jetbrains.compose.material:material-icons-extended-uikitarm64:${libs.versions.icons.get()}")
}

val portedLicenseArtifacts = configurations.create("portedLicenseArtifacts") {
    isCanBeConsumed = true
    isCanBeResolved = false
    attributes { attribute(Usage.USAGE_ATTRIBUTE, objects.named("kotlin-api")) }
}
sourceCoordinates.values.forEach { coordinate ->
    val dependency = dependencies.create("$coordinate:sources@jar") as org.gradle.api.artifacts.ExternalModuleDependency
    dependency.isTransitive = false
    dependencies.add(portedLicenseArtifacts.name, dependency)
}

val extractSources = tasks.register("extractSources")
val sourceTasks = sourceCoordinates.mapValues { (name, coordinate) ->
    val sourceArchive = configurations.create("${name.replace("-", "")}SourceArchive") {
        isTransitive = false
        attributes { attribute(Usage.USAGE_ATTRIBUTE, objects.named("kotlin-api")) }
    }
    dependencies.add(sourceArchive.name, "$coordinate:sources@jar")
    tasks.register<Sync>("extract${name.replace("-", "")}Source") {
        inputs.file(buildFile)
        from(provider { sourceArchive.files.map { zipTree(it) } })
        into(layout.buildDirectory.dir("sources/$name"))
        if (name == "preference") {
            inputs.property("resourceContentHashSupported", false)
            filesMatching("commonMain/**/generated/resources/*.kt") {
                // 新版资源访问器的热重载标记不属于鸿蒙 Compose 1.9 的 API。
                filter { line: String ->
                    if (line.startsWith("import org.jetbrains.compose.resources.ResourceContentHash") ||
                        line.startsWith("@delegate:ResourceContentHash")) "" else line
                }
            }
        }
        if (name == "icons-extended") {
            val appSources = rootProject.file("../../composeApp/src/commonMain/kotlin")
            inputs.dir(appSources)
            doFirst {
                val pattern = Regex("import (androidx\\.compose\\.material\\.icons\\.[A-Za-z.]+)")
                val paths = appSources.walkTopDown().filter { it.extension == "kt" }.flatMap { file ->
                    pattern.findAll(file.readText()).map { "commonMain/${it.groupValues[1].replace('.', '/')}.kt" }
                }.toSet()
                setIncludes(paths)
            }
        }
    }.also { extractSources.configure { dependsOn(it) } }
}

val collectLibraryResources = tasks.register<Sync>("collectLibraryResources") {
    into(layout.buildDirectory.dir("composeResources"))
}
sourceCoordinates.filterKeys { it.startsWith("sheets-") || it == "preference" || it == "toast" }.forEach { (name, coordinate) ->
    val resources = configurations.create("${name.replace("-", "")}ResourceArchive") {
        isTransitive = false
        attributes { attribute(Usage.USAGE_ATTRIBUTE, objects.named("kotlin-multiplatformresources")) }
    }
    dependencies.add(resources.name, coordinate)
    collectLibraryResources.configure {
        from(provider { resources.files.map { zipTree(it) } })
    }
}

kotlin {
    val architecture = providers.gradleProperty("harmonyArch").orElse("arm64").get()
    require(architecture in setOf("arm64", "x64")) { "Unsupported harmonyArch: $architecture" }
    val ohosTarget = if (architecture == "x64") ohosX64() else ohosArm64()
    compilerOptions {
        optIn.add("kotlin.time.ExperimentalTime")
        optIn.add("kotlinx.cinterop.ExperimentalForeignApi")
        optIn.add("androidx.compose.material3.ExperimentalMaterial3Api")
        optIn.add("androidx.compose.material3.ExperimentalMaterial3ExpressiveApi")
        optIn.add("androidx.compose.ui.ExperimentalComposeUiApi")
    }
    sourceSets {
        commonMain {
            sourceTasks.values.forEach { source ->
                kotlin.srcDir(source.map { it.destinationDir.resolve("commonMain") })
            }
            dependencies {
                api(compose.runtime)
                api(compose.foundation)
                api(compose.animationGraphics)
                api(compose.material3)
                api(compose.ui)
                api(compose.components.resources)
                api(libs.coroutines)
                api(libs.serialization)
                api(libs.datetime)
                api(libs.io)
                api(libs.ktor.core)
                api(libs.lifecycle)
                api(libs.coil.core)
                api(libs.coil.compose)
            }
        }
        val nonWebMain by creating {
            dependsOn(commonMain.get())
            listOf("filekit", "filekit-coil", "filekit-dialogs").forEach { name ->
                kotlin.srcDir(sourceTasks.getValue(name).map { it.destinationDir.resolve("nonWebMain") })
            }
            kotlin.srcDir(sourceTasks.getValue("filekit-dialogs").map { it.destinationDir.resolve("mobileMain") })
        }
        val nativeIoMain by creating {
            dependsOn(nonWebMain)
            kotlin.srcDir(sourceTasks.getValue("filekit").map { it.destinationDir.resolve("jvmAndNativeMain") })
        }
        val ohosMain by creating {
            dependsOn(nativeIoMain)
            sourceTasks.filterKeys { it.startsWith("sheets-") }.values.forEach { source ->
                kotlin.srcDir(source.map { it.destinationDir.resolve("nonJvmMain") })
                kotlin.srcDir(source.map { it.destinationDir.resolve("iosArm64Main") })
            }
            listOf("preference", "toast").forEach { name ->
                kotlin.srcDir(sourceTasks.getValue(name).map { it.destinationDir.resolve("iosArm64Main") })
            }
        }
        getByName("${ohosTarget.name}Main").dependsOn(ohosMain)
    }
}
