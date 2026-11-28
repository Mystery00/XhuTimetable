plugins {
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.kotlinSerialization) apply false
}

val simulator = providers.gradleProperty("harmonyArch").orElse("arm64").get() == "x64"
val simulatorVersions = mapOf(
    "io.insert-koin" to libs.versions.simulatorKoin.get(),
    "app.cash.paging" to libs.versions.simulatorPaging.get(),
    "org.kotlincrypto.core" to libs.versions.simulatorCryptoCore.get(),
    "org.kotlincrypto.bitops" to libs.versions.simulatorBitops.get(),
)
val simulatorKermit = libs.versions.simulatorKermit.get()
val simulatorStately = libs.versions.simulatorStately.get()
val simulatorHash = libs.versions.simulatorHash.get()
val simulatorSha1 = libs.versions.simulatorSha1.get()
val simulatorCryptoError = libs.versions.simulatorCryptoError.get()

subprojects {
    val nativeProject = name != "loggerChecks"
    configurations.configureEach {
        if (simulator && nativeProject) {
            resolutionStrategy.eachDependency {
                val version = when {
                    requested.group == "co.touchlab" && requested.name.startsWith("kermit") -> simulatorKermit
                    requested.group == "co.touchlab" && requested.name.startsWith("stately") -> simulatorStately
                    requested.group == "org.kotlincrypto.hash" && requested.name.startsWith("sha1") -> simulatorSha1
                    requested.group == "org.kotlincrypto.hash" &&
                        (requested.name.startsWith("md") || requested.name.startsWith("sha2")) -> simulatorHash
                    requested.group == "org.kotlincrypto" && requested.name.startsWith("error") -> simulatorCryptoError
                    else -> simulatorVersions[requested.group]
                }
                if (version != null) useVersion(version)
            }
        }
    }
}
