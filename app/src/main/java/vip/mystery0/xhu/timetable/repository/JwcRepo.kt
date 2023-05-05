package vip.mystery0.xhu.timetable.repository

import org.koin.java.KoinJavaComponent
import vip.mystery0.xhu.timetable.api.JwcApi
import vip.mystery0.xhu.timetable.api.checkLogin
import vip.mystery0.xhu.timetable.config.store.User
import vip.mystery0.xhu.timetable.config.store.UserStore.withAutoLogin
import vip.mystery0.xhu.timetable.isOnline
import vip.mystery0.xhu.timetable.model.request.ClassroomRequest
import vip.mystery0.xhu.timetable.model.response.ClassroomResponse
import vip.mystery0.xhu.timetable.module.NetworkNotConnectException

suspend fun getCourseRoomList(
    user: User,
    location: String,
    week: List<Int>,
    day: List<Int>,
    time: List<Int>,
): List<ClassroomResponse> {
    if (!isOnline()) {
        throw NetworkNotConnectException()
    }
    val request = ClassroomRequest(location, week, day, time)
    val jwcApi = KoinJavaComponent.get<JwcApi>(JwcApi::class.java)
    val response = user.withAutoLogin {
        jwcApi.courseRoomList(it, request).checkLogin()
    }
    return response.first
}