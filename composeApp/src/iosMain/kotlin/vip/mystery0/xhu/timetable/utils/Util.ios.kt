package vip.mystery0.xhu.timetable.utils

import co.touchlab.kermit.Logger
import multiplatform.network.cmptoast.showToast
import platform.SystemConfiguration.SCNetworkReachabilityCreateWithName
import platform.SystemConfiguration.SCNetworkReachabilityFlags
import platform.SystemConfiguration.SCNetworkReachabilityGetFlags
import platform.UIKit.UIPasteboard
import platform.posix.kSCNetworkFlagsConnectionRequired
import platform.posix.kSCNetworkFlagsReachable

actual fun isOnline(): Boolean {
    val reachability = SCNetworkReachabilityCreateWithName(null, "www.apple.com")
    if (reachability == null) {
        Logger.w("isOnline: SCNetworkReachabilityCreateWithName returned null")
        return false
    }

    var flags: SCNetworkReachabilityFlags = 0u
    if (!SCNetworkReachabilityGetFlags(reachability, flags.ptr)) {
        Logger.w("isOnline: SCNetworkReachabilityGetFlags returned false")
        return false
    }

    val isReachable = (flags and kSCNetworkFlagsReachable) != 0u
    val needsConnection = (flags and kSCNetworkFlagsConnectionRequired) != 0u

    val isOnline = isReachable && !needsConnection
    Logger.i("isOnline: $isOnline, flags: $flags")
    return isOnline
}

actual fun copyToClipboard(text: String) {
    val pasteboard = UIPasteboard.generalPasteboard
    pasteboard.string = text
    showToast("文本已经复制到剪切板")
}

actual fun forceExit() {
    Logger.i("force exit in ios")
    kotlin.system.exitProcess(10)
}