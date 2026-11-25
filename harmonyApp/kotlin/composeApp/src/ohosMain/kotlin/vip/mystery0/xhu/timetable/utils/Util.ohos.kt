package vip.mystery0.xhu.timetable.utils

import vip.mystery0.xhu.timetable.platform.OhosPlatform

actual fun isOnline(): Boolean = OhosPlatform.networkConnected
actual fun copyToClipboard(text: String) = OhosPlatform.launch("copyText", text)
actual fun forceExit() = OhosPlatform.launch("terminate")
