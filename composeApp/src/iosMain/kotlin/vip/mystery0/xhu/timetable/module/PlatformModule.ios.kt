package vip.mystery0.xhu.timetable.module

import androidx.room.RoomDatabase
import androidx.sqlite.driver.NativeSQLiteDriver
import co.touchlab.kermit.Logger
import io.ktor.client.engine.darwin.DarwinHttpRequestException
import org.koin.core.module.Module
import org.koin.dsl.module
import platform.Foundation.NSError
import vip.mystery0.xhu.timetable.db.AppDatabase

actual fun platformModule(): Module = module {
    single<RoomDatabase.Builder<AppDatabase>> {
        getDatabaseBuilder()
            .setDriver(NativeSQLiteDriver())
    }
}

actual fun Throwable.desc(): String {
    if (this is NSError) {
        return this.localizedDescription
    }
    if (this is DarwinHttpRequestException) {
        return this.origin.localizedDescription
    }
    if (this.message != null) {
        return this.message!!
    }
    Logger.w("throwable class name: ${this::class.simpleName}")
    if (this.cause != null) {
        Logger.w("throwable cause class name: ${this.cause!!::class.simpleName}")
    }
    val stackTraceToString = this.stackTraceToString()
    Logger.w("throwable stack trace: $stackTraceToString")
    return this::class.simpleName?.let {
        "捕获异常：${it}"
    } ?: stackTraceToString
}