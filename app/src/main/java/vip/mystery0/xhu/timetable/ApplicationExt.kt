package vip.mystery0.xhu.timetable

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.Application
import android.app.PendingIntent
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Environment
import android.provider.Settings
import android.util.Log
import androidx.browser.customtabs.CustomTabsIntent
import com.microsoft.appcenter.AppCenter
import com.microsoft.appcenter.analytics.Analytics
import com.microsoft.appcenter.crashes.Crashes
import vip.mystery0.xhu.timetable.base.BaseComposeActivity
import vip.mystery0.xhu.timetable.config.GlobalConfig
import vip.mystery0.xhu.timetable.config.chinaZone
import vip.mystery0.xhu.timetable.config.getConfig
import vip.mystery0.xhu.timetable.work.NotifyService
import java.io.File
import java.time.LocalDate
import java.time.LocalTime

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

@Suppress("DEPRECATION")
fun isOnline(): Boolean {
    val connMgr = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val networkInfo = connMgr.activeNetworkInfo
    return networkInfo?.isConnected == true
}

val externalPictureDir: File
    get() = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!
val externalDownloadDir: File
    get() = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)!!
val externalDocumentsDir: File
    get() = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)!!

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
        AppCenter.setUserId(publicDeviceId)
        AppCenter.start(
            application,
            "463eb268-c114-49de-880c-c596cf5be561",
            Analytics::class.java,
            Crashes::class.java
        )
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

suspend fun setTrigger(alarmManager: AlarmManager) {
    val notifyCourse = getConfig { notifyCourse }
    val notifyExam = getConfig { notifyExam }
    if (!notifyCourse && !notifyExam) {
        return
    }
    val notifyTime = getConfig { notifyTime } ?: return
    val now = LocalTime.now()
    val alarmIntent = Intent(context, NotifyService::class.java)
    val pendingIntent = PendingIntent.getForegroundService(
        context,
        0,
        alarmIntent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
    //关闭定时器
    alarmManager.cancel(pendingIntent)
    //设置新的定时器
    val triggerAtTime =
        if (now.isBefore(notifyTime)) notifyTime.atDate(LocalDate.now())
        else notifyTime.atDate(LocalDate.now().plusDays(1))
    alarmManager.set(
        AlarmManager.RTC_WAKEUP,
        triggerAtTime.atZone(chinaZone).toInstant().toEpochMilli(),
        pendingIntent
    )
}