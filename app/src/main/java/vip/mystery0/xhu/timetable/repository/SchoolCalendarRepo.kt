package vip.mystery0.xhu.timetable.repository

import org.koin.java.KoinJavaComponent
import vip.mystery0.xhu.timetable.api.ServerApi
import vip.mystery0.xhu.timetable.api.checkLogin
import vip.mystery0.xhu.timetable.config.store.UserStore.withAutoLogin
import vip.mystery0.xhu.timetable.config.store.User
import vip.mystery0.xhu.timetable.config.runOnIo
import vip.mystery0.xhu.timetable.model.response.SchoolCalendarResponse

suspend fun getSchoolCalendarList(user: User): List<SchoolCalendarResponse> {
    val response = runOnIo {
        val serverApi = KoinJavaComponent.get<ServerApi>(ServerApi::class.java)
        user.withAutoLogin {
            serverApi.schoolCalendarList(it).checkLogin()
        }
    }
    return response.first
}

suspend fun getSchoolCalendarUrl(user: User, resourceId: Long): String {
    val response = runOnIo {
        val serverApi = KoinJavaComponent.get<ServerApi>(ServerApi::class.java)
        user.withAutoLogin {
            serverApi.schoolCalendarUrl(it, resourceId).checkLogin()
        }
    }
    return response.first.url
}