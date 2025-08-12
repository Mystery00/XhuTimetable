package vip.mystery0.xhu.timetable

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import co.touchlab.kermit.Logger
import co.touchlab.kermit.koin.KermitKoinLogger
import com.tencent.mmkv.MMKV
import com.tencent.mmkv.MMKVLogLevel
import multiplatform.network.cmptoast.AppContext
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import vip.mystery0.xhu.timetable.config.logger.FileLogWriter
import vip.mystery0.xhu.timetable.config.mmkv.KermitMMKVLogger
import vip.mystery0.xhu.timetable.module.moduleList
import vip.mystery0.xhu.timetable.utils.ApplicationExceptionCatcher

@SuppressLint("StaticFieldLeak")
internal lateinit var context: Context

class Application : Application(), DefaultLifecycleObserver {
    private lateinit var fileLogWriter: FileLogWriter

    override fun onCreate() {
        super<Application>.onCreate()
        context = this
        Thread.setDefaultUncaughtExceptionHandler(ApplicationExceptionCatcher(Thread.getDefaultUncaughtExceptionHandler()))
        fileLogWriter = initLogger()
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

    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
        Logger.i("App is in the background. Stopping FileLogWriter to flush logs.")
        if (::fileLogWriter.isInitialized) {
            fileLogWriter.stop()
        }
    }
}