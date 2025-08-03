package vip.mystery0.xhu.timetable.utils

import platform.UIKit.UIPasteboard

actual fun isOnline(): Boolean = true

actual fun copyToClipboard(text: String) {
    val pasteboard = UIPasteboard.generalPasteboard
    pasteboard.string = text
}

actual fun forceExit() {
    kotlin.system.exitProcess(10)
}