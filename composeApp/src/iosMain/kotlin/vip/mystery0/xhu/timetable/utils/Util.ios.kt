package vip.mystery0.xhu.timetable.utils

import platform.UIKit.UIPasteboard

actual fun isOnline(): Boolean = true // TODO: Implement actual network check for iOS

actual fun copyToClipboard(text: String) {
    val pasteboard = UIPasteboard.generalPasteboard
    pasteboard.string = text
}