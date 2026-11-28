plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.aboutLibraries)
}

val architecture = providers.gradleProperty("harmonyArch").orElse("arm64").get()
require(architecture in setOf("arm64", "x64")) { "Unsupported harmonyArch: $architecture" }
val ohosName = if (architecture == "x64") "ohosX64" else "ohosArm64"
val ohosTaskSuffix = ohosName.replaceFirstChar { it.uppercase() }

val portedLicenses by configurations.creating {
    isCanBeConsumed = false
    attributes { attribute(Usage.USAGE_ATTRIBUTE, objects.named("kotlin-api")) }
}

val prepareSharedSources by tasks.registering(Sync::class) {
    from(rootProject.file("../../composeApp/src/commonMain/kotlin"))
    exclude(
        "**/Helper.kt", "**/App.kt", "**/base/ComposeScreen.kt", "**/model/event/MenuNavigator.kt",
        "**/ui/screen/**", "**/ui/pager/**", "**/ui/component/**", "**/ui/navigation/**",
        "**/ui/theme/Builder.kt", "**/ui/theme/Color.kt", "**/ui/theme/Font.kt",
        "**/ui/theme/Icon.kt", "**/ui/theme/SystemAppearance.kt", "**/ui/theme/Theme.kt",
    )
    into(layout.buildDirectory.dir("generated/sharedSources"))
    filteringCharset = "UTF-8"
    // 鸿蒙 Room 适配使用 room3 命名空间，共享实体与 SQL 仍来自原源码。
    inputs.file(buildFile)
    inputs.property("roomApi", libs.versions.room.get())
    filter { line: String -> line.replace("import androidx.room.", "import androidx.room3.") }
    filesMatching("**/module/DatabaseModule.kt") {
        filter { line: String -> line.replace("override fun migrate(", "override suspend fun migrate(") }
    }
    filesMatching("**/db/DB.kt") {
        filter { line: String ->
            line.replace(Regex("\\bTypeConverters?\\b")) { "Column${it.value}" }
        }
    }
}

val prepareFeatureClient by tasks.registering(Sync::class) {
    from(rootProject.file("../../composeApp/src/iosMain/kotlin")) {
        include("**/Feature.ios.kt")
        rename("Feature.ios.kt", "FeatureClient.ohos.kt")
        filter { line: String -> if (line.trimStart().startsWith("actual fun platform()")) "" else line }
    }
    into(layout.buildDirectory.dir("generated/featureClient"))
}

kotlin {
    compilerOptions {
        optIn.add("kotlin.time.ExperimentalTime")
        optIn.add("kotlin.uuid.ExperimentalUuidApi")
        optIn.add("kotlinx.cinterop.ExperimentalForeignApi")
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }
    listOf(if (architecture == "x64") ohosX64() else ohosArm64()).forEach { target ->
        target.binaries.sharedLib {
            baseName = "kn"
            linkerOpts("-lz")
        }
    }

    sourceSets {
        commonMain {
            kotlin.srcDir(prepareSharedSources.map { it.destinationDir })
        }
        commonMain.dependencies {
            implementation(project(":portedLibraries"))
            implementation(libs.serialization)
            implementation(libs.datetime)
            implementation(libs.atomicfu)
            implementation(libs.viewmodel)
            implementation(libs.koin.core)
            implementation(libs.koin.viewmodel)
            implementation(libs.kermit)
            implementation(libs.hash.md)
            implementation(libs.hash.sha1)
            implementation(libs.hash.sha2)
            implementation(libs.paging.common)
            implementation(libs.room)
            implementation(libs.ktor.core)
            implementation(libs.ktor.websockets)
            implementation(libs.ktor.negotiation)
            implementation(libs.ktor.json)
            implementation(libs.coroutines)
        }
        val ohosMain by creating {
            kotlin.srcDir(prepareFeatureClient.map { it.destinationDir })
            dependsOn(commonMain.get())
            dependencies {
                implementation(libs.sqlite)
            }
        }
        getByName("${ohosName}Main").apply {
            dependsOn(ohosMain)
            kotlin.srcDir(rootProject.file("../../composeApp/src/platformMain/kotlin"))
        }
    }
}

dependencies {
    add(portedLicenses.name, project(path = ":portedLibraries", configuration = "portedLicenseArtifacts"))
    add("ksp$ohosTaskSuffix", libs.room.compiler)
    add("ksp$ohosTaskSuffix", libs.ktorfit.compiler)
}

ksp {
    arg("room.schemaLocation", layout.buildDirectory.dir("schemas").get().asFile.absolutePath)
    arg("room.generateKotlin", "true")
}

val licenseJson = layout.buildDirectory.file("generated/licenses/aboutlibraries.json")
aboutLibraries {
    offlineMode = true
    collect {
        all = true
        filterVariants.addAll("${ohosName}CompileKlibraries", "portedLicenses")
        fetchRemoteLicense = false
        fetchRemoteFunding = false
    }
    export {
        outputFile = licenseJson.get().asFile
    }
}

val harmonyRoot = rootProject.file("..")
val sharedResources = rootProject.file("../../composeApp/src/commonMain/composeResources")
val syncHarmonyResources by tasks.registering {
    dependsOn("exportLibraryDefinitions")
    inputs.file(licenseJson)
    inputs.dir(sharedResources)
    outputs.dir(harmonyRoot.resolve("entry/src/main/resources/rawfile/composeResources"))
    outputs.dir(harmonyRoot.resolve("entry/src/main/resources/rawfile/shared"))
    doLast {
        // 两个目录只包含构建生成的资源，重新导出时同步移除源端已删除的 XML 和 SVG。
        val rawResources = harmonyRoot.resolve("entry/src/main/resources/rawfile")
        val nativeResources = rawResources.resolve("shared")
        delete(rawResources.resolve("composeResources"), nativeResources)
        copy {
            from(licenseJson)
            into(harmonyRoot.resolve("entry/src/main/resources/rawfile/composeResources/xhutimetable.composeapp.generated.resources/files"))
        }
        val factory = javax.xml.parsers.DocumentBuilderFactory.newInstance().apply {
            setFeature("http://apache.org/xml/features/disallow-doctype-decl", true)
        }
        nativeResources.mkdirs()
        copy {
            from(sharedResources.resolve("drawable"))
            exclude("*.xml")
            into(nativeResources)
        }
        fun color(value: String): Pair<String, Double> = when {
            value.length == 9 && value.startsWith("#") -> "#${value.substring(3)}" to (value.substring(1, 3).toInt(16) / 255.0)
            value.isBlank() -> "none" to 1.0
            else -> value to 1.0
        }
        fun render(element: org.w3c.dom.Element, tint: String): String {
            fun attribute(name: String, default: String = "") = element.getAttribute("android:$name").ifBlank { default }
            fun number(name: String, default: Double = 0.0) = attribute(name).toDoubleOrNull() ?: default
            return when (element.tagName) {
                "group", "vector" -> buildString {
                    if (element.tagName == "group") {
                        val px = number("pivotX")
                        val py = number("pivotY")
                        append("<g transform=\"translate(${number("translateX") + px} ${number("translateY") + py}) rotate(${number("rotation")}) scale(${number("scaleX", 1.0)} ${number("scaleY", 1.0)}) translate(${-px} ${-py})\">")
                    }
                    for (index in 0 until element.childNodes.length) {
                        val child = element.childNodes.item(index)
                        if (child is org.w3c.dom.Element) append(render(child, tint))
                    }
                    if (element.tagName == "group") append("</g>")
                }
                "path" -> buildString {
                    val (fill, fillAlpha) = color(attribute("fillColor"))
                    val (stroke, strokeAlpha) = color(attribute("strokeColor"))
                    append("<path d=\"${attribute("pathData")}\" fill=\"${if (tint.isNotBlank() && fill != "none") tint else fill}\" fill-opacity=\"${fillAlpha * number("fillAlpha", 1.0)}\"")
                    append(" fill-rule=\"${if (attribute("fillType") == "evenOdd") "evenodd" else "nonzero"}\"")
                    if (stroke != "none") {
                        append(" stroke=\"${tint.ifBlank { stroke }}\" stroke-opacity=\"${strokeAlpha * number("strokeAlpha", 1.0)}\" stroke-width=\"${attribute("strokeWidth", "0")}\"")
                        append(" stroke-linecap=\"${attribute("strokeLineCap", "butt")}\" stroke-linejoin=\"${attribute("strokeLineJoin", "miter")}\"")
                    }
                    append("/>")
                }
                else -> ""
            }
        }
        sharedResources.resolve("drawable").listFiles().orEmpty().filter { it.extension == "xml" && it.name != "ic_no_data.xml" }.forEach { resource ->
            val vector = factory.newDocumentBuilder().parse(resource).documentElement
            if (vector.tagName == "vector") {
                val width = vector.getAttribute("android:viewportWidth")
                val height = vector.getAttribute("android:viewportHeight")
                val content = render(vector, vector.getAttribute("android:tint"))
                nativeResources.resolve("${resource.nameWithoutExtension}.svg").writeText(
                    "<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"$width\" height=\"$height\" viewBox=\"0 0 $width $height\">$content</svg>"
                )
            }
        }
    }
}

listOf(ohosTaskSuffix to if (architecture == "x64") "x86_64" else "arm64-v8a").forEach { (target, abi) ->
    listOf("Debug" to "debugShared", "Release" to "releaseShared").forEach { (variant, binDirName) ->
        tasks.register<Copy>("publish$variant$target") {
            dependsOn("link${variant}Shared$target", syncHarmonyResources)
            from(layout.buildDirectory.dir("bin/${target.replaceFirstChar { it.lowercase() }}/$binDirName")) {
                include("libkn.so")
                into("libs/$abi")
            }
            from(layout.buildDirectory.dir("bin/${target.replaceFirstChar { it.lowercase() }}/$binDirName")) {
                include("libkn_api.h")
                into("src/main/cpp/include/$abi")
            }
            into(harmonyRoot.resolve("entry"))
        }
    }
}
