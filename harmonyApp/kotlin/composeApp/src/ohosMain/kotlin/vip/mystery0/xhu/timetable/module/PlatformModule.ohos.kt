package vip.mystery0.xhu.timetable.module

import androidx.room3.Room
import androidx.room3.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.absolutePath
import io.github.vinceglb.filekit.databasesDir
import org.koin.core.module.Module
import org.koin.dsl.module
import vip.mystery0.xhu.timetable.db.AppDatabase

actual fun platformModule(): Module = module {
    single<RoomDatabase.Builder<AppDatabase>> {
        Room.databaseBuilder<AppDatabase>(
            name = PlatformFile(FileKit.databasesDir, "$DATABASE_NAME.db").absolutePath(),
        ).setDriver(BundledSQLiteDriver())
    }
}

actual fun Throwable.desc(): String = message ?: "操作失败，请重试"
actual fun platformViewModelModule(module: Module) = Unit
