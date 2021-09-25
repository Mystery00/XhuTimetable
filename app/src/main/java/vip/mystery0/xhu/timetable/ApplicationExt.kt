package vip.mystery0.xhu.timetable

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.os.Environment
import android.provider.Settings
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

val screenWidth: Int
    get() = context.resources.displayMetrics.widthPixels
val screenHeight: Int
    get() = context.resources.displayMetrics.heightPixels