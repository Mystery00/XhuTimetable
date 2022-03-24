package vip.mystery0.xhu.timetable.repository

import org.koin.java.KoinJavaComponent
import vip.mystery0.xhu.timetable.api.JwcApi
import vip.mystery0.xhu.timetable.api.checkLogin
import vip.mystery0.xhu.timetable.config.SessionManager.withAutoLogin
import vip.mystery0.xhu.timetable.config.User
import vip.mystery0.xhu.timetable.config.getConfig
import vip.mystery0.xhu.timetable.isOnline
import vip.mystery0.xhu.timetable.model.request.CourseRoomRequest
import vip.mystery0.xhu.timetable.model.response.CourseRoomResponse
import vip.mystery0.xhu.timetable.model.response.ExamResponse
import vip.mystery0.xhu.timetable.model.response.ExpScoreResponse
import vip.mystery0.xhu.timetable.model.response.ScoreResponse
import vip.mystery0.xhu.timetable.module.NetworkNotConnectException

suspend fun getExamList(user: User): ExamResponse {
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

suspend fun getScoreList(
    user: User,
    year: String,
    term: Int,
): ScoreResponse {
    if (!isOnline()) {
        throw NetworkNotConnectException()
    }
    val jwcApi = KoinJavaComponent.get<JwcApi>(JwcApi::class.java)
    val response = user.withAutoLogin {
        jwcApi.scoreList(it, year, term).checkLogin()
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