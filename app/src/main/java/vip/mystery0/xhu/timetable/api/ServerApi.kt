package vip.mystery0.xhu.timetable.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import vip.mystery0.xhu.timetable.config.interceptor.ServerNeedLoginException
import vip.mystery0.xhu.timetable.model.UserInfo
import vip.mystery0.xhu.timetable.model.request.InitRequest
import vip.mystery0.xhu.timetable.model.request.LoginRequest
import vip.mystery0.xhu.timetable.model.response.CourseResponse
import vip.mystery0.xhu.timetable.model.response.InitResponse
import vip.mystery0.xhu.timetable.model.response.LoginResponse
import vip.mystery0.xhu.timetable.model.response.PublicKeyResponse

interface ServerApi {
    @POST("/api/rest/xhu-timetable/common/init")
    suspend fun initRequest(@Body initRequest: InitRequest): InitResponse

    @GET("/api/rest/xhu-timetable/server/publicKey")
    suspend fun publicKey(): PublicKeyResponse

    @POST("/api/rest/xhu-timetable/login")
    suspend fun login(@Body loginRequest: LoginRequest): LoginResponse

    @GET("/api/rest/xhu-timetable/server/user")
    suspend fun userInfo(@Header("token") token: String): UserInfo

    @GET("/api/rest/xhu-timetable/server/course/list")
    suspend fun courseList(@Header("token") token: String): Response<List<CourseResponse>>
}

fun <T : Any> Response<T>.checkLogin(): T {
    if (code() == 401) {
        throw ServerNeedLoginException()
    }
    return body()!!
}