import org.gradle.internal.impldep.org.joda.time.LocalDate

object BuildVersionConfig {
    val nightly: Boolean
        get() = System.getenv("NIGHTLY")?.toBoolean() ?: false
}