package vip.mystery0.xhu.timetable.utils

import co.touchlab.kermit.Logger
import platform.UIKit.UIPasteboard
import vip.mystery0.xhu.timetable.config.toast.showShortToast

actual fun isOnline(): Boolean = true

actual fun copyToClipboard(text: String) {
    val pasteboard = UIPasteboard.generalPasteboard
    pasteboard.string = text
    showShortToast("文本已经复制到剪切板")
}

actual fun forceExit() {
    Logger.i("force exit in ios")
    kotlin.system.exitProcess(10)
}