package vip.mystery0.xhu.timetable.utils

import co.touchlab.kermit.Logger
import kotlinx.cinterop.alloc
import kotlinx.cinterop.ExperimentalForeignApi
import multiplatform.network.cmptoast.showToast
import platform.SystemConfiguration.SCNetworkReachabilityCreateWithName
import platform.SystemConfiguration.SCNetworkReachabilityGetFlags
import platform.SystemConfiguration.kSCNetworkFlagsConnectionRequired
import platform.SystemConfiguration.kSCNetworkFlagsReachable
import platform.UIKit.UIPasteboard
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.value

@OptIn(ExperimentalForeignApi::class)
actual fun isOnline(): Boolean {
    val reachability = SCNetworkReachabilityCreateWithName(null, "www.apple.com")
    if (reachability == null) {
        Logger.w("isOnline: SCNetworkReachabilityCreateWithName returned null")
        return false
    }

    return memScoped {
        val flags = alloc<SCNetworkReachabilityFlagsVar>()
        if (!SCNetworkReachabilityGetFlags(reachability, flags.ptr)) {
            Logger.w("isOnline: SCNetworkReachabilityGetFlags returned false")
            return false
        }

        val isReachable = (flags.value and kSCNetworkFlagsReachable) != 0u
        val needsConnection = (flags.value and kSCNetworkFlagsConnectionRequired) != 0u

        val isOnline = isReachable && !needsConnection
        Logger.i("isOnline: $isOnline, flags: ${flags.value}")
        isOnline
    }
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