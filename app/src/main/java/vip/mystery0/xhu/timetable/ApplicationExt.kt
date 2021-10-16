package vip.mystery0.xhu.timetable

import android.annotation.SuppressLint
import android.app.Application
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import android.util.Log
import androidx.browser.customtabs.CustomTabsIntent
import com.microsoft.appcenter.AppCenter
import com.microsoft.appcenter.analytics.Analytics
import com.microsoft.appcenter.crashes.AbstractCrashesListener
import com.microsoft.appcenter.crashes.Crashes
import com.microsoft.appcenter.crashes.model.ErrorReport
import vip.mystery0.xhu.timetable.base.BaseComposeActivity
import vip.mystery0.xhu.timetable.config.GlobalConfig
import java.io.File

@SuppressLint("StaticFieldLeak")
internal lateinit var context: Context

//设备id
val publicDeviceId: String
    @SuppressLint("HardwareIds")
    get() = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)

//应用名称
val appName: String
    get() = context.getString(R.string.app_name)

//应用包名
val packageName: String
    get() = BuildConfig.APPLICATION_ID

//版本名称
val appVersionName: String by lazy { context.getString(R.string.app_version_name) }

//版本号
val appVersionCode: String by lazy { context.getString(R.string.app_version_code) }
val appVersionCodeNumber: Long by lazy { appVersionCode.toLong() }

@SuppressLint("deprecation")
fun isOnline(): Boolean {
    val connMgr = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val networkInfo = connMgr.activeNetworkInfo
    return networkInfo?.isConnected == true
}

val externalPictureDir: File by lazy { context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)!! }
val externalDownloadDir: File by lazy { context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)!! }
val externalDocumentsDir: File by lazy { context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)!! }

fun BaseComposeActivity.toCustomTabs(url: String) {
    try {
        val builder = CustomTabsIntent.Builder()
        val intent = builder.build()
        intent.launchUrl(this, Uri.parse(url))
    } catch (e: Exception) {
        loadInBrowser(url)
    }
}

fun BaseComposeActivity.loadInBrowser(url: String) {
    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        "请先安装一个浏览器。".toast(true)
    }
}

fun registerAppCenter(application: Application) {
    if (!AppCenter.isConfigured() && GlobalConfig.allowSendCrashReport) {
        if (BuildConfig.DEBUG) {
            AppCenter.setLogLevel(Log.VERBOSE)
        }
        Crashes.setListener(object : AbstractCrashesListener() {
            override fun onBeforeSending(report: ErrorReport?) {
                if (report == null) {
                    return
                }
                try {
                    dumpStackTraceToFile(report.appStartTime.time, report.stackTrace)
                    Log.d(packageName, "onBeforeSending: write exception to file success")
                } catch (e: Exception) {
                    Log.w(packageName, "onBeforeSending: dump stackTrace failed", e)
                }
            }
        })
        AppCenter.setUserId(publicDeviceId)
        AppCenter.start(
            application,
            "463eb268-c114-49de-880c-c596cf5be561",
            Analytics::class.java,
            Crashes::class.java
        )
    }
}

private fun dumpStackTraceToFile(timestamp: Long, stackTrace: String) {
    val dumpDir = File(externalDocumentsDir, "crash")
    val dumpFile = File(dumpDir, "Crash-${timestamp}.txt")
    if (!dumpDir.exists()) dumpDir.mkdirs()
    val content = buildString {
        appendLine("===================================")
        appendLine("应用版本: $appVersionName")
        appendLine("Android版本: ${Build.VERSION.RELEASE}_${Build.VERSION.SDK_INT}")
        appendLine("厂商: ${Build.MANUFACTURER}")
        appendLine("型号: ${Build.MODEL}")
        appendLine("设备id: $publicDeviceId")
        appendLine("===================================")
        appendLine()
        appendLine(stackTrace)
    }
    dumpFile.writeText(content)
}