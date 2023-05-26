package vip.mystery0.xhu.timetable

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.Application
import android.app.PendingIntent
import android.content.ActivityNotFoundException
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Environment
import android.os.PowerManager
import android.provider.Settings
import android.util.Log
import android.webkit.WebSettings
import androidx.browser.customtabs.CustomTabsIntent
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.microsoft.appcenter.AppCenter
import com.microsoft.appcenter.analytics.Analytics
import com.microsoft.appcenter.crashes.AbstractCrashesListener
import com.microsoft.appcenter.crashes.Crashes
import com.microsoft.appcenter.crashes.ingestion.models.ErrorAttachmentLog
import com.microsoft.appcenter.crashes.model.ErrorReport
import org.koin.java.KoinJavaComponent
import vip.mystery0.xhu.timetable.base.BaseComposeActivity
import vip.mystery0.xhu.timetable.config.store.GlobalConfigStore
import vip.mystery0.xhu.timetable.config.store.UserStore
import vip.mystery0.xhu.timetable.config.store.getConfigStore
import vip.mystery0.xhu.timetable.utils.asInstant
import vip.mystery0.xhu.timetable.work.NotifyService
import vip.mystery0.xhu.timetable.work.NotifyWork
import java.io.File
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

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
const val appVersionCodeNumber: Long = BuildConfig.VERSION_CODE.toLong()

//系统UA
val userAgent: String
    get() = WebSettings.getDefaultUserAgent(context)

//更新日志
val updateLogArray: Array<String>
    get() = context.resources.getStringArray(R.array.update_log)

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
    if (GlobalConfigStore.allowSendCrashReport) {
        if (BuildConfig.DEBUG) {
            AppCenter.setLogLevel(Log.VERBOSE)
        }
        Crashes.setListener(object : AbstractCrashesListener() {
            override fun getErrorAttachments(report: ErrorReport): MutableIterable<ErrorAttachmentLog> {
                try {
                    val loggedUserList = UserStore.blockLoggedUserList()
                    val list = loggedUserList.map {
                        mapOf(
                            "studentId" to it.studentId,
                            "token" to it.token,
                        )
                    }
                    val attachment = ErrorAttachmentLog.attachmentWithText(
                        "loggedUserList: $list}",
                        "userInfo.txt"
                    )
                    return mutableListOf(attachment)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                return mutableListOf()
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

fun trackEvent(event: String) {
    if (AppCenter.isConfigured() && GlobalConfigStore.allowSendCrashReport) {
        Analytics.trackEvent(event)
    }
}

fun trackError(error: Throwable) {
    if (AppCenter.isConfigured() && GlobalConfigStore.allowSendCrashReport) {
        Crashes.trackError(error)
    }
}

fun Context.joinQQGroup(activity: BaseComposeActivity) {
    try {
        val goIntent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("mqqopensdkapi://bizAgent/qm/qr?url=http%3A%2F%2Fqm.qq.com%2Fcgi-bin%2Fqm%2Fqr%3Ffrom%3Dapp%26p%3Dandroid%26k%3DJwFjXuBEWPJuevlxn9QhtMRGFSh-geZV")
        )
        startActivity(goIntent)
    } catch (e: ActivityNotFoundException) {
        activity.toastString("QQ未安装", true)
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