package vip.mystery0.xhu.timetable.utils

import java.time.Instant

private var lastClick = 0L

fun isTwiceClick(): Boolean {
    val nowClick = Instant.now().toEpochMilli()
    if (nowClick - lastClick >= 1000) {
        lastClick = nowClick
        return false
    }
    return true
}