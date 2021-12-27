package vip.mystery0.xhu.timetable.repository

import org.koin.java.KoinJavaComponent
import vip.mystery0.xhu.timetable.api.ServerApi
import vip.mystery0.xhu.timetable.api.checkLogin
import vip.mystery0.xhu.timetable.config.SessionManager.withAutoLogin
import vip.mystery0.xhu.timetable.config.User
import vip.mystery0.xhu.timetable.model.CustomCourse

suspend fun getCustomCourseList(
    user: User,
    year: String,
    term: Int,
): List<CustomCourse> {
    val serverApi = KoinJavaComponent.get<ServerApi>(ServerApi::class.java)
    val response = user.withAutoLogin {
        serverApi.customCourseList(it, year, term).checkLogin()
    }
    return response.first
}