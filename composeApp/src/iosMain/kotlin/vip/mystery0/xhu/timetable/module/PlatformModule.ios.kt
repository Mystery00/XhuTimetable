package vip.mystery0.xhu.timetable.module

import androidx.room.RoomDatabase
import androidx.sqlite.driver.NativeSQLiteDriver
import org.koin.core.module.Module
import org.koin.dsl.module
import vip.mystery0.xhu.timetable.db.AppDatabase

actual fun platformModule(): Module = module {
    single<RoomDatabase.Builder<AppDatabase>> {
        getDatabaseBuilder()
            .setDriver(NativeSQLiteDriver())
    }
}

actual fun Throwable.desc(): String = this::class.simpleName?.let {
    "捕获异常：${it}"
} ?: this.stackTraceToString()