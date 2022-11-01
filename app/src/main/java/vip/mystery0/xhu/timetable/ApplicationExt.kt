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
import vip.mystery0.xhu.timetable.config.GlobalConfig
import vip.mystery0.xhu.timetable.config.SessionManager
import vip.mystery0.xhu.timetable.config.chinaZone
import vip.mystery0.xhu.timetable.config.getConfig
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
    if (GlobalConfig.allowSendCrashReport) {
        if (BuildConfig.DEBUG) {
            AppCenter.setLogLevel(Log.VERBOSE)
        }
        Crashes.setListener(object : AbstractCrashesListener() {
            override fun getErrorAttachments(report: ErrorReport): MutableIterable<ErrorAttachmentLog> {
                try {
                    val loggedUserList = GlobalConfig.userList
                    val list = loggedUserList.map {
                        mapOf(
                            "studentId" to it.studentId,
                            "token" to it.token,
                            "main" to it.main,
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
    if (AppCenter.isConfigured() && GlobalConfig.allowSendCrashReport) {
        Analytics.trackEvent(event)
    }
}

fun trackError(error: Throwable) {
    if (AppCenter.isConfigured() && GlobalConfig.allowSendCrashReport) {
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

suspend fun setAlarmTrigger(
    alarmManager: AlarmManager,
    executeTime: LocalDateTime? = null,
) {
    val notifyCourse = getConfig { notifyCourse }
    val notifyExam = getConfig { notifyExam }
    if (!notifyCourse && !notifyExam) {
        return
    }
    val alarmIntent = Intent(context, NotifyService::class.java)
    val pendingIntent = PendingIntent.getForegroundService(
        context,
        0,
        alarmIntent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
    //关闭定时器
    alarmManager.cancel(pendingIntent)

    val now = LocalDateTime.now()
    val nextExecuteTime = if (executeTime == null) {
        val notifyTime = getConfig { notifyTime } ?: return
        var time = notifyTime.atDate(LocalDate.now())
        if (time.isBefore(now)) {
            //当天计算出来的时间比当前时间早，那么调度时间改成明天
            time = time.plusDays(1)
        }
        time
    } else {
        executeTime
    }

    alarmManager.cancel(pendingIntent)
    alarmManager.set(
        AlarmManager.RTC_WAKEUP,
        nextExecuteTime.atZone(chinaZone).toInstant().toEpochMilli(),
        pendingIntent
    )
    Log.i("ApplicationExt", "set alarm trigger success, next execute time: $nextExecuteTime")
}

suspend fun setTrigger(workManager: WorkManager, executeTime: LocalDateTime? = null) {
    val notifyCourse = getConfig { notifyCourse }
    val notifyExam = getConfig { notifyExam }
    if (!notifyCourse && !notifyExam) {
        return
    }
    val uniqueWorkName = NotifyWork::class.java.name
    if (executeTime == null) {
        workManager.cancelUniqueWork(uniqueWorkName)
    }

    val now = LocalDateTime.now()
    val nextExecuteTime = if (executeTime == null) {
        val notifyTime = getConfig { notifyTime } ?: return
        var time = notifyTime.atDate(LocalDate.now())
        if (time.isBefore(now)) {
            //当天计算出来的时间比当前时间早，那么调度时间改成明天
            time = time.plusDays(1)
        }
        time
    } else {
        executeTime
    }
    val duration = Duration.between(now, nextExecuteTime)
    workManager.enqueueUniqueWork(
        uniqueWorkName,
        ExistingWorkPolicy.KEEP,
        OneTimeWorkRequestBuilder<NotifyWork>()
            .setInitialDelay(duration.toMillis(), TimeUnit.MILLISECONDS)
            .build()
    )
    Log.i("ApplicationExt", "work enqueue success, next execute time: $nextExecuteTime")
}

fun isIgnoringBatteryOptimizations(): Boolean {
    var isIgnoring = false
    val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager?
    if (powerManager != null) {
        isIgnoring = powerManager.isIgnoringBatteryOptimizations(packageName)
    }
    return isIgnoring
}