plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.kotlinSerialization)
}

val sourceCoordinates = mapOf(
    "filekit" to "io.github.vinceglb:filekit-core-iosarm64:${libs.versions.filekit.get()}",
    "filekit-dialogs" to "io.github.vinceglb:filekit-dialogs-iosarm64:${libs.versions.filekit.get()}",
    "ktorfit" to "de.jensklingenberg.ktorfit:ktorfit-lib-light-iosarm64:${libs.versions.ktorfit.get()}",
    "ktorfit-annotations" to "de.jensklingenberg.ktorfit:ktorfit-annotations-iosarm64:${libs.versions.ktorfit.get()}",
)

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
        if (name == "filekit") {
            filesMatching("nonWebMain/**/FileKit.nonWeb.kt") {
                filter { line: String ->
                    if (line.trim() == "import androidx.annotation.IntRange") ""
                    else line.replace("@IntRange(from = 0, to = 100) ", "")
                }
            }
        }
    }.also { extractSources.configure { dependsOn(it) } }
}

kotlin {
    val architecture = providers.gradleProperty("harmonyArch").orElse("arm64").get()
    require(architecture in setOf("arm64", "x64")) { "Unsupported harmonyArch: $architecture" }
    val ohosTarget = if (architecture == "x64") ohosX64() else ohosArm64()
    compilerOptions {
        optIn.add("kotlin.time.ExperimentalTime")
        optIn.add("kotlinx.cinterop.ExperimentalForeignApi")
    }
    sourceSets {
        commonMain {
            sourceTasks.values.forEach { source ->
                kotlin.srcDir(source.map { it.destinationDir.resolve("commonMain") })
            }
            dependencies {
                api(libs.coroutines)
                api(libs.serialization)
                api(libs.datetime)
                api(libs.io)
                api(libs.ktor.core)
            }
        }
        val nonWebMain by creating {
            dependsOn(commonMain.get())
            listOf("filekit", "filekit-dialogs").forEach { name ->
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
        }
        getByName("${ohosTarget.name}Main").dependsOn(ohosMain)
    }
}
