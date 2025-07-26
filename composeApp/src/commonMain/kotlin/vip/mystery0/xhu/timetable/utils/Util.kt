package vip.mystery0.xhu.timetable.utils

import kotlin.time.Clock

private var lastClick = 0L

fun isTwiceClick(): Boolean {
    val nowClick = Clock.System.now().toEpochMilliseconds()
    if (nowClick - lastClick >= 1000) {
        lastClick = nowClick
        return false
    }
    return true
}

internal fun Int.pad2(): String = toString().padStart(2, '0')
internal fun Int.pad3(): String = toString().padStart(2, '0')

expect fun isOnline(): Boolean

expect fun copyToClipboard(text: String)

expect fun forceExit()