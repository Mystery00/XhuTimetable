package vip.mystery0.xhu.timetable.repository

import org.koin.java.KoinJavaComponent
import vip.mystery0.xhu.timetable.api.JwcApi
import vip.mystery0.xhu.timetable.api.checkLogin
import vip.mystery0.xhu.timetable.config.SessionManager.withAutoLogin
import vip.mystery0.xhu.timetable.config.User
import vip.mystery0.xhu.timetable.model.response.ExamResponse

suspend fun getExamList(user: User): ExamResponse {
    val jwcApi = KoinJavaComponent.get<JwcApi>(JwcApi::class.java)
    val response = user.withAutoLogin {
        jwcApi.examList(it).checkLogin()
    }
    return response.first
}