package vip.mystery0.xhu.timetable.repository

import org.koin.java.KoinJavaComponent
import vip.mystery0.xhu.timetable.api.ServerApi
import vip.mystery0.xhu.timetable.api.checkLogin
import vip.mystery0.xhu.timetable.config.SessionManager.withAutoLogin
import vip.mystery0.xhu.timetable.config.User
import vip.mystery0.xhu.timetable.isOnline
import vip.mystery0.xhu.timetable.model.response.UrgeResponse
import vip.mystery0.xhu.timetable.module.NetworkNotConnectException

suspend fun getUrgeList(user: User): UrgeResponse {
    if (!isOnline()) {
        throw NetworkNotConnectException()
    }
    val serverApi = KoinJavaComponent.get<ServerApi>(ServerApi::class.java)
    val response = user.withAutoLogin {
        serverApi.getUrgeList(it).checkLogin()
    }
    return response.first
}

suspend fun doUrge(user: User, urgeId: Long) {
    if (!isOnline()) {
        throw NetworkNotConnectException()
    }
    val serverApi = KoinJavaComponent.get<ServerApi>(ServerApi::class.java)
    user.withAutoLogin {
        serverApi.urge(it, urgeId).checkLogin()
    }
}