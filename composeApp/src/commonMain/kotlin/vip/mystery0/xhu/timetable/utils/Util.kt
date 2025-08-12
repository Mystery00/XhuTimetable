package vip.mystery0.xhu.timetable.utils

internal fun Int.pad2(): String = toString().padStart(2, '0')
internal fun Int.pad3(): String = toString().padStart(2, '0')

expect fun isOnline(): Boolean

expect fun copyToClipboard(text: String)

expect fun forceExit()