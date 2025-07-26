package vip.mystery0.xhu.timetable.repository

import org.koin.core.component.inject
import vip.mystery0.xhu.timetable.api.CalendarApi
import vip.mystery0.xhu.timetable.base.BaseDataRepo
import vip.mystery0.xhu.timetable.config.store.UserStore.withAutoLoginOnce
import vip.mystery0.xhu.timetable.model.response.SchoolCalendarResponse

object SchoolCalendarRepo : BaseDataRepo {
    private val calendarApi: CalendarApi by inject()

    suspend fun getList(): List<SchoolCalendarResponse> {
        checkForceLoadFromCloud(true)

        return mainUser().withAutoLoginOnce {
            calendarApi.selectAllBackground(it)
        }
    }
}