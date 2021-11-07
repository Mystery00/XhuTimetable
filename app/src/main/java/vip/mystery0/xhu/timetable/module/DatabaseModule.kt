package vip.mystery0.xhu.timetable.module

import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module
import vip.mystery0.xhu.timetable.repository.db.DB

val databaseModule = module {
    single {
        Room.databaseBuilder(androidApplication(), DB::class.java, "xhu-timetable")
            .addMigrations(MIGRATION_1_2)
            .build()
    }
    single { get<DB>().courseDao() }
    single { get<DB>().courseColorDao() }
    single { get<DB>().noticeDao() }
}

private val MIGRATION_1_2: Migration = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("CREATE TABLE IF NOT EXISTS `tb_course_color` (`courseName` TEXT NOT NULL, `color` TEXT NOT NULL, `id` INTEGER PRIMARY KEY AUTOINCREMENT)")
    }
}