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
            .addMigrations(MIGRATION_2_3)
            .build()
    }
    single { get<DB>().courseDao() }
    single { get<DB>().courseColorDao() }
    single { get<DB>().noticeDao() }
    single { get<DB>().customThingDao() }
}

private val MIGRATION_1_2: Migration = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("CREATE TABLE IF NOT EXISTS `tb_course_color` (`courseName` TEXT NOT NULL, `color` TEXT NOT NULL, `id` INTEGER PRIMARY KEY AUTOINCREMENT)")
    }
}

private val MIGRATION_2_3: Migration = object : Migration(2, 3) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("CREATE TABLE IF NOT EXISTS `tb_custom_thing` (`thingId` INTEGER NOT NULL, `title` TEXT NOT NULL, `location` TEXT NOT NULL, `allDay` INTEGER NOT NULL, `startTime` INTEGER NOT NULL, `endTime` INTEGER NOT NULL, `remark` TEXT NOT NULL, `color` TEXT NOT NULL, `extraData` TEXT NOT NULL, `year` TEXT NOT NULL, `term` INTEGER NOT NULL, `studentId` TEXT NOT NULL, PRIMARY KEY(`thingId`))")
    }
}