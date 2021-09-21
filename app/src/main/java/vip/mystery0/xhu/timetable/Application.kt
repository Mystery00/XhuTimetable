package vip.mystery0.xhu.timetable

import android.app.Application
import com.tencent.mmkv.MMKV
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.androidx.workmanager.koin.workManagerFactory
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
            workManagerFactory()
            modules(moduleList())
        }
        MMKV.initialize(this)
    }
}