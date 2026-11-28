package androidx.compose.runtime

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.BINARY)
annotation class Immutable

fun <T> mutableStateListOf(vararg elements: T): MutableList<T> = elements.toMutableList()
