package vip.mystery0.xhu.timetable.repository

import org.koin.java.KoinJavaComponent
import vip.mystery0.xhu.timetable.api.JwcApi
import vip.mystery0.xhu.timetable.api.checkLogin
import vip.mystery0.xhu.timetable.config.store.UserStore.withAutoLogin
import vip.mystery0.xhu.timetable.config.store.User
import vip.mystery0.xhu.timetable.config.getConfig
import vip.mystery0.xhu.timetable.isOnline
import vip.mystery0.xhu.timetable.model.request.CourseRoomRequest
import vip.mystery0.xhu.timetable.model.response.CourseRoomResponse
import vip.mystery0.xhu.timetable.model.response.ExamResponse11
import vip.mystery0.xhu.timetable.model.response.ExpScoreResponse
import vip.mystery0.xhu.timetable.module.NetworkNotConnectException

suspend fun getExamList(user: User): ExamResponse11 {
    if (!isOnline()) {
        throw NetworkNotConnectException()
    }
    val jwcApi = KoinJavaComponent.get<JwcApi>(JwcApi::class.java)
    val year = getConfig { currentYear }
    val term = getConfig { currentTerm }
    val response = user.withAutoLogin {
        jwcApi.examList(it, year, term).checkLogin()
    }
    return response.first
}

suspend fun getExpScoreList(
    user: User,
    year: String,
    term: Int,
): List<ExpScoreResponse> {
    if (!isOnline()) {
        throw NetworkNotConnectException()
    }
    val jwcApi = KoinJavaComponent.get<JwcApi>(JwcApi::class.java)
    val response = user.withAutoLogin {
        jwcApi.expScoreList(it, year, term).checkLogin()
    }
    return response.first
}

suspend fun getCourseRoomList(
    user: User,
    location: String,
    week: List<Int>,
    day: List<Int>,
    time: List<Int>,
): List<CourseRoomResponse> {
    if (!isOnline()) {
        throw NetworkNotConnectException()
    }
    val request = CourseRoomRequest(location, week, day, time)
    val jwcApi = KoinJavaComponent.get<JwcApi>(JwcApi::class.java)
    val response = user.withAutoLogin {
        jwcApi.courseRoomList(it, request).checkLogin()
    }
    return response.first
}