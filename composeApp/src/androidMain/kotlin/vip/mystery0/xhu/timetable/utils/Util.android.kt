package vip.mystery0.xhu.timetable.utils

import android.content.ClipData
import android.content.ClipboardManager
import android.net.ConnectivityManager
import android.widget.Toast
import org.koin.java.KoinJavaComponent
import vip.mystery0.xhu.timetable.base.appName
import vip.mystery0.xhu.timetable.context
import kotlin.system.exitProcess

@Suppress("DEPRECATION")
actual fun isOnline(): Boolean {
    val connectivityManager =
        KoinJavaComponent.get<ConnectivityManager>(ConnectivityManager::class.java)
    val networkInfo = connectivityManager.activeNetworkInfo
    return networkInfo?.isConnected == true
}

actual fun copyToClipboard(text: String) {
    val clipboardManager =
        KoinJavaComponent.get<ClipboardManager>(ClipboardManager::class.java)
    val clipData = ClipData.newPlainText(appName(), text)
    clipboardManager.setPrimaryClip(clipData)
}

actual fun forceExit() {
    Toast.makeText(context.applicationContext, "检测到异常，即将关闭应用", Toast.LENGTH_LONG)
        .show()
    android.os.Process.killProcess(android.os.Process.myPid())
    exitProcess(10)
}