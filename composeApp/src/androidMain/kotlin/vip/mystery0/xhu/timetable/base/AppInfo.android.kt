package vip.mystery0.xhu.timetable.base

import android.annotation.SuppressLint
import android.os.Build
import android.provider.Settings
import vip.mystery0.xhu.timetable.BuildConfig
import vip.mystery0.xhu.timetable.R
import vip.mystery0.xhu.timetable.context

actual fun systemVersion(): String = "Android ${Build.VERSION.RELEASE}-${Build.VERSION.SDK_INT}"
actual fun deviceFactory(): String = Build.MANUFACTURER
actual fun deviceModel(): String = Build.MODEL
actual fun deviceRom(): String = Build.DISPLAY

//设备id
private val publicDeviceId: String
    @SuppressLint("HardwareIds")
    get() = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)

//应用名称
private val appName: String
    get() = context.getString(R.string.app_name)

actual fun publicDeviceId(): String = "android-${publicDeviceId}"

actual fun appName(): String = appName

actual fun packageName(): String = BuildConfig.APPLICATION_ID

actual fun appVersionName(): String = BuildConfig.VERSION_NAME

actual fun appVersionCode(): String = BuildConfig.VERSION_CODE.toString()

actual fun appVersionCodeNumber(): Long = BuildConfig.VERSION_CODE.toLong()