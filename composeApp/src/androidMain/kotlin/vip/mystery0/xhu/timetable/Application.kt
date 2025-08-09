package vip.mystery0.xhu.timetable

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import co.touchlab.kermit.Logger
import co.touchlab.kermit.koin.KermitKoinLogger
import com.tencent.mmkv.MMKV
import com.tencent.mmkv.MMKVLogLevel
import multiplatform.network.cmptoast.AppContext
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import vip.mystery0.xhu.timetable.config.mmkv.KermitMMKVLogger
import vip.mystery0.xhu.timetable.feature.FeatureHub
import vip.mystery0.xhu.timetable.module.moduleList
import vip.mystery0.xhu.timetable.utils.ApplicationExceptionCatcher

@SuppressLint("StaticFieldLeak")
internal lateinit var context: Context

class Application : Application() {
    override fun onCreate() {
        super.onCreate()
        context = this
        Thread.setDefaultUncaughtExceptionHandler(ApplicationExceptionCatcher(Thread.getDefaultUncaughtExceptionHandler()))
        initLogger()
        initFeature()
        startKoin {
            logger(KermitKoinLogger(Logger.withTag("koin")))
            androidContext(this@Application)
            modules(moduleList())
        }
        AppContext.apply { set(applicationContext) }
        val root = context.filesDir.absolutePath + "/mmkv"
        MMKV.initialize(this, root, null, MMKVLogLevel.LevelWarning, KermitMMKVLogger())
    }

    override fun onTerminate() {
        FeatureHub.stop()
        super.onTerminate()
    }
}