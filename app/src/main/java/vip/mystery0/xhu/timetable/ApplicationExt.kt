package vip.mystery0.xhu.timetable

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Environment
import android.provider.Settings
import androidx.browser.customtabs.CustomTabsIntent
import vip.mystery0.xhu.timetable.base.BaseComposeActivity
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