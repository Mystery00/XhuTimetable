package vip.mystery0.xhu.timetable.repository

import org.koin.java.KoinJavaComponent
import vip.mystery0.xhu.timetable.api.ServerApi
import vip.mystery0.xhu.timetable.api.checkLogin
import vip.mystery0.xhu.timetable.config.SessionManager.withAutoLogin
import vip.mystery0.xhu.timetable.config.User
import vip.mystery0.xhu.timetable.model.response.ExamResponse

suspend fun getExamList(user: User): ExamResponse {
    val serverApi = KoinJavaComponent.get<ServerApi>(ServerApi::class.java)
    val response = user.withAutoLogin {
        serverApi.examList(it).checkLogin()
    }
    return response.first
}