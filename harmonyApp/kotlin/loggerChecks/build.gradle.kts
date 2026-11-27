plugins {
    id("org.jetbrains.kotlin.jvm")
}

kotlin {
    jvmToolchain(21)
    compilerOptions { optIn.add("kotlin.time.ExperimentalTime") }
    sourceSets.named("main") {
        kotlin.srcDir(rootProject.file("composeApp/src/commonMain/kotlin"))
        kotlin.include("**/NativeProtocol.kt")
    }
}

dependencies {
    implementation(libs.coroutines)
    implementation(libs.datetime)
    implementation(libs.kermit)
    implementation(libs.serialization)
    testImplementation(libs.kotlin.test.junit)
}
