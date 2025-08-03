package vip.mystery0.xhu.timetable.utils

import co.touchlab.kermit.Logger
import platform.UIKit.UIPasteboard

actual fun isOnline(): Boolean = true

actual fun copyToClipboard(text: String) {
    val pasteboard = UIPasteboard.generalPasteboard
    pasteboard.string = text
}

actual fun forceExit() {
    Logger.i("force exit in ios")
    kotlin.system.exitProcess(10)
}