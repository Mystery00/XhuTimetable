package vip.mystery0.xhu.timetable

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.provider.Settings
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import vip.mystery0.xhu.timetable.module.moduleList

class Application : Application() {
    override fun onCreate() {
        super.onCreate()
        context = this
        //配置Koin
        startKoin {
            androidLogger(Level.ERROR)
            androidContext(this@Application)
            modules(moduleList())
        }
    }
}

@SuppressLint("StaticFieldLeak")
private lateinit var context: Context

//设备id
val publicDeviceId: String
    @SuppressLint("HardwareIds")
    get() = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)

//版本名称
val appVersionName: String by lazy { context.getString(R.string.app_version_name) }

//版本号
val appVersionCode: String by lazy { context.getString(R.string.app_version_code) }

@SuppressLint("deprecation")
fun isOnline(): Boolean {
    val connMgr = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val networkInfo = connMgr.activeNetworkInfo
    return networkInfo?.isConnected == true
}