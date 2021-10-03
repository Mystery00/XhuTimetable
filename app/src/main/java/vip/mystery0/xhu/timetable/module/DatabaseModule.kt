package vip.mystery0.xhu.timetable.module

import androidx.room.Room
import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module
import vip.mystery0.xhu.timetable.repository.db.DB

val databaseModule = module {
    single {
        Room.databaseBuilder(androidApplication(), DB::class.java, "xhu-timetable")
            .build()
    }
    single { get<DB>().courseDao() }
    single { get<DB>().noticeDao() }
}