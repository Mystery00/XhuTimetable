package vip.mystery0.xhu.timetable.module

import android.app.AlarmManager
import android.app.NotificationManager
import android.content.ClipboardManager
import android.content.Context
import androidx.work.WorkManager
import org.greenrobot.eventbus.EventBus
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

fun moduleList(): List<Module> =
    listOf(
        appModule,
        databaseModule,
        workModule,
        networkModule,
        repoModule,
    )

private val appModule = module {
    single { WorkManager.getInstance(androidContext()) }
    single { androidContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager }
    single { androidContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager }
    single { androidContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager }
    single { EventBus.getDefault() }
}