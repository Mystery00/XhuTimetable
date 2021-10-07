package vip.mystery0.xhu.timetable.api

import retrofit2.Response
import retrofit2.http.*
import vip.mystery0.xhu.timetable.config.ServerError
import vip.mystery0.xhu.timetable.config.interceptor.ServerNeedLoginException
import vip.mystery0.xhu.timetable.config.parseServerError
import vip.mystery0.xhu.timetable.model.UserInfo
import vip.mystery0.xhu.timetable.model.request.InitRequest
import vip.mystery0.xhu.timetable.model.request.LoginRequest
import vip.mystery0.xhu.timetable.model.response.*

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
    suspend fun courseList(
        @Header("token") token: String,
        @Query("year") year: String,
        @Query("term") term: Int,
    ): Response<List<CourseResponse>>

    @GET("/api/rest/xhu-timetable/server/notice")
    suspend fun noticeList(
        @Header("token") token: String,
        @Query("platform") platform: String = "Android"
    ): Response<List<NoticeResponse>>

    @GET("/api/rest/xhu-timetable/server/exam/list")
    suspend fun examList(@Header("token") token: String): Response<ExamResponse>
}

fun <T : Any> Response<T>.checkLogin(): T {
    if (code() == 401) {
        throw ServerNeedLoginException()
    }
    if (code() >= 400) {
        val response = errorBody()?.string()
        if (response != null) {
            parseServerError(response)?.let {
                throw ServerError(it.message)
            }
        }
    }
    return body()!!
}