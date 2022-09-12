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
            .addMigrations(MIGRATION_3_4)
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

private val MIGRATION_3_4: Migration = object : Migration(3, 4) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("alter table tb_course_item rename to _temp_tb_course_item")
        database.execSQL("CREATE TABLE IF NOT EXISTS `tb_course_item` (`courseName` TEXT NOT NULL, `teacherName` TEXT NOT NULL, `location` TEXT NOT NULL, `weekString` TEXT NOT NULL, `weekNum` INTEGER NOT NULL, `time` TEXT NOT NULL, `weekIndex` INTEGER NOT NULL, `type` INTEGER NOT NULL, `source` INTEGER NOT NULL, `extraData` TEXT NOT NULL, `year` TEXT NOT NULL, `term` INTEGER NOT NULL, `studentId` TEXT NOT NULL, `id` INTEGER PRIMARY KEY AUTOINCREMENT)")
        database.execSQL("insert into tb_course_item select courseName, teacherName, location, weekString, weekNum, time, weekIndex, type, source, '[]', year, term, studentId, id from _temp_tb_course_item")
        database.execSQL("drop table _temp_tb_course_item")
    }
}