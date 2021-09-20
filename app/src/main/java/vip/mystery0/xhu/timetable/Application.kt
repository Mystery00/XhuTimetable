package vip.mystery0.xhu.timetable

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.provider.Settings
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import vip.mystery0.xhu.timetable.module.moduleList

class Application : Application() {
    override fun onCreate() {
        super.onCreate()
        //配置Koin
        startKoin {
            androidLogger(Level.ERROR)
            androidContext(this@Application)
            modules(moduleList())
        }
    }
}

val Context.publicDeviceId: String
    @SuppressLint("HardwareIds")
    get() = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)