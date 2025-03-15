package vip.mystery0.xhu.timetable

import android.annotation.SuppressLint
import android.app.Application
import android.content.ActivityNotFoundException
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Environment
import android.os.PowerManager
import android.provider.Settings
import android.webkit.WebSettings
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.net.toUri
import org.koin.java.KoinJavaComponent
import vip.mystery0.xhu.timetable.base.BaseComposeActivity
import vip.mystery0.xhu.timetable.config.store.GlobalConfigStore
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
const val packageName: String = BuildConfig.APPLICATION_ID

//版本名称
const val appVersionName: String = BuildConfig.VERSION_NAME

//版本号
const val appVersionCode: String = BuildConfig.VERSION_CODE.toString()

//系统UA
val userAgent: String
    get() = WebSettings.getDefaultUserAgent(context)

@Suppress("DEPRECATION")
fun isOnline(): Boolean {
    val connectivityManager =
        KoinJavaComponent.get<ConnectivityManager>(ConnectivityManager::class.java)
    val networkInfo = connectivityManager.activeNetworkInfo
    return networkInfo?.isConnected == true
}

val externalPictureDir: File
    get() = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!
val externalDownloadDir: File
    get() = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)!!
val externalCacheDownloadDir: File
    get() = File(context.externalCacheDir, "update").apply {
        if (!exists()) {
            mkdirs()
        }
    }
val externalDocumentsDir: File
    get() = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)!!
val contentResolver: ContentResolver
    get() = context.contentResolver

val customImageDir: File
    get() = File(externalPictureDir, "custom").apply {
        if (!exists()) {
            mkdirs()
        }
    }

val screenWidth: Int
    get() = context.resources.displayMetrics.widthPixels
val screenHeight: Int
    get() = context.resources.displayMetrics.heightPixels

fun doClear() {
    //clear dir
    externalDownloadDir.deleteRecursively()
}

fun BaseComposeActivity.toCustomTabs(url: String) {
    if (url.isBlank()) {
        "跳转地址不能为空".toast(true)
        return
    }
    try {
        val builder = CustomTabsIntent.Builder()
        val intent = builder.build()
        intent.launchUrl(this, url.toUri())
    } catch (e: Exception) {
        loadInBrowser(url)
    }
}

fun BaseComposeActivity.loadInBrowser(url: String) {
    try {
        val intent = Intent(Intent.ACTION_VIEW, url.toUri()).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        "请先安装一个浏览器。".toast(true)
    }
}

fun registerAppCenter(application: Application) {
    if (GlobalConfigStore.allowSendCrashReport) {
//        Crashes.setListener(object : AbstractCrashesListener() {
//            override fun getErrorAttachments(report: ErrorReport): MutableIterable<ErrorAttachmentLog> {
//                try {
//                    val loggedUserList = UserStore.blockLoggedUserList()
//                    val list = loggedUserList.map {
//                        mapOf(
//                            "studentId" to it.studentId,
//                            "token" to it.token,
//                        )
//                    }
//                    val attachment = ErrorAttachmentLog.attachmentWithText(
//                        "loggedUserList: $list}",
//                        "userInfo.txt"
//                    )
//                    return mutableListOf(attachment)
//                } catch (e: Exception) {
//                    e.printStackTrace()
//                }
//                return mutableListOf()
//            }
//        })
    }
}

fun trackEvent(event: String) {
//    if (AppCenter.isConfigured() && GlobalConfigStore.allowSendCrashReport) {
//        Analytics.trackEvent(event)
//    }
}

fun trackError(error: Throwable) {
//    if (AppCenter.isConfigured() && GlobalConfigStore.allowSendCrashReport) {
//        Crashes.trackError(error)
//    }
}

fun BaseComposeActivity.joinQQGroup(loadInBrowser: Boolean) {
    val url = "https://blog.mystery0.vip/xgkb-group"
    if (loadInBrowser) {
        loadInBrowser((url))
    } else {
        toCustomTabs(url)
    }
}

fun isIgnoringBatteryOptimizations(): Boolean {
    var isIgnoring = false
    val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager?
    if (powerManager != null) {
        isIgnoring = powerManager.isIgnoringBatteryOptimizations(packageName)
    }
    return isIgnoring
}