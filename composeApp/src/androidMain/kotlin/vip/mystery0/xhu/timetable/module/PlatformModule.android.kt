package vip.mystery0.xhu.timetable.module

import android.app.AlarmManager
import android.app.NotificationManager
import android.content.ClipboardManager
import android.content.Context
import android.net.ConnectivityManager
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.driver.AndroidSQLiteDriver
import androidx.work.WorkManager
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module
import vip.mystery0.xhu.timetable.db.AppDatabase

actual fun platformModule(): Module = module {
    single { WorkManager.getInstance(androidContext()) }
    single { androidContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager }
    single { androidContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager }
    single { androidContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager }
    single { androidContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager }

    single<RoomDatabase.Builder<AppDatabase>> {
        getDatabaseBuilder(get())
            .setDriver(AndroidSQLiteDriver())
    }
}

private fun getDatabaseBuilder(ctx: Context): RoomDatabase.Builder<AppDatabase> {
    val appContext = ctx.applicationContext
    return Room.databaseBuilder(
        context = appContext,
        klass = AppDatabase::class.java,
        name = DATABASE_NAME
    )
}

actual fun Throwable.desc(): String = this.message ?: this.javaClass.simpleName