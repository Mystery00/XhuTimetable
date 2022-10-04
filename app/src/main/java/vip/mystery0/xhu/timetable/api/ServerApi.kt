package vip.mystery0.xhu.timetable.api

import retrofit2.Response
import retrofit2.http.*
import vip.mystery0.xhu.timetable.config.ServerError
import vip.mystery0.xhu.timetable.config.interceptor.ServerNeedLoginException
import vip.mystery0.xhu.timetable.config.parseServerError
import vip.mystery0.xhu.timetable.model.CustomCourse
import vip.mystery0.xhu.timetable.model.UserInfo
import vip.mystery0.xhu.timetable.model.request.*
import vip.mystery0.xhu.timetable.model.response.*

interface ServerApi {
    @POST("/api/rest/xhu-timetable/common/init")
    suspend fun initRequest(@Body initRequest: InitRequest): InitResponse

    @GET("/api/rest/xhu-timetable/common/version/url")
    suspend fun versionUrl(@Query("versionId") versionId: Long): VersionUrl

    @GET("/api/rest/xhu-timetable/server/publicKey")
    suspend fun publicKey(): PublicKeyResponse

    @POST("/api/rest/xhu-timetable/login")
    suspend fun login(@Body loginRequest: LoginRequest): LoginResponse

    @GET("/api/rest/xhu-timetable/server/user")
    suspend fun userInfo(@Header("token") token: String): UserInfo

    @GET("/api/rest/xhu-timetable/server/notice")
    suspend fun noticeList(
        @Header("token") token: String,
        @Query("platform") platform: String = "Android"
    ): Response<List<NoticeResponse>>

    @GET("/api/rest/xhu-timetable/server/schoolCalendar")
    suspend fun schoolCalendarList(@Header("token") token: String): Response<List<SchoolCalendarResponse>>

    @GET("/api/rest/xhu-timetable/server/schoolCalendarUrl")
    suspend fun schoolCalendarUrl(
        @Header("token") token: String,
        @Query("resourceId") resourceId: Long,
    ): Response<ResourceUrlResponse>

    @GET("/api/rest/xhu-timetable/server/customCourse")
    suspend fun customCourseList(
        @Header("token") token: String,
        @Query("year") year: String,
        @Query("term") term: Int,
    ): Response<List<CustomCourse>>

    @POST("/api/rest/xhu-timetable/server/customCourse")
    suspend fun createCustomCourse(
        @Header("token") token: String,
        @Body customCourseRequest: CustomCourseRequest
    ): Response<Boolean>

    @PUT("/api/rest/xhu-timetable/server/customCourse")
    suspend fun updateCustomCourse(
        @Header("token") token: String,
        @Query("id") id: Long,
        @Body customCourseRequest: CustomCourseRequest
    ): Response<Boolean>

    @DELETE("/api/rest/xhu-timetable/server/customCourse")
    suspend fun deleteCustomCourse(
        @Header("token") token: String,
        @Query("id") id: Long,
    ): Response<Boolean>

    @GET("/api/rest/xhu-timetable/server/customThing")
    suspend fun customThingList(
        @Header("token") token: String,
        @Query("year") year: String,
        @Query("term") term: Int,
    ): Response<List<CustomThingResponse>>

    @POST("/api/rest/xhu-timetable/server/customThing")
    suspend fun createCustomThing(
        @Header("token") token: String,
        @Body customThingRequest: CustomThingRequest
    ): Response<Boolean>

    @PUT("/api/rest/xhu-timetable/server/customThing")
    suspend fun updateCustomThing(
        @Header("token") token: String,
        @Query("id") id: Long,
        @Body customThingRequest: CustomThingRequest
    ): Response<Boolean>

    @DELETE("/api/rest/xhu-timetable/server/customThing")
    suspend fun deleteCustomThing(
        @Header("token") token: String,
        @Query("id") id: Long,
    ): Response<Boolean>

    @POST("/api/rest/xhu-timetable/server/allCourse")
    suspend fun selectAllCourse(
        @Header("token") token: String,
        @Body allCourseRequest: AllCourseRequest,
    ): Response<List<AllCourseResponse>>

    @GET("/api/rest/xhu-timetable/server/background")
    suspend fun selectAllBackground(
        @Header("token") token: String,
    ): Response<List<BackgroundResponse>>

    @GET("/api/rest/xhu-timetable/server/background/url")
    suspend fun getBackgroundUrl(
        @Header("token") token: String,
        @Query("resourceId") resourceId: Long,
    ): Response<ResourceUrlResponse>

    @GET("/api/rest/xhu-timetable/common/teamMember")
    suspend fun getTeamMemberList(): List<TeamMemberResponse>

    @GET("/api/rest/xhu-timetable/server/calendar")
    suspend fun getCalendarEventList(
        @Header("token") token: String,
        @Query("year") year: String,
        @Query("term") term: Int,
        @Query("includeCustomCourse") includeCustomCourse: Boolean,
        @Query("includeCustomThing") includeCustomThing: Boolean,
    ): Response<List<CalendarEventResponse>>

    @GET("/api/rest/xhu-timetable/server/urge")
    suspend fun getUrgeList(
        @Header("token") token: String,
    ): Response<UrgeResponse>

    @PUT("/api/rest/xhu-timetable/server/urge")
    suspend fun urge(
        @Header("token") token: String,
        @Query("urgeId") urgeId: Long,
    ): Response<Unit>

    @GET("/api/rest/xhu-timetable/health/check")
    suspend fun serverDetect(): Response<Unit>

    @GET("/api/rest/xhu-timetable/push/fetch")
    suspend fun fetchPush(@Header("token") token: String): Response<List<PushResponse>>

    @POST("/api/rest/xhu-timetable/server/academicReport")
    suspend fun getAcademicReportList(
        @Header("token") token: String,
        @Body keywords: AcademicReportRequest,
    ): Response<List<AcademicReportResponse>>
}

fun <T : Any> Response<T>.checkLogin(): T {
    val httpCode = code()
    if (httpCode == 401) {
        throw ServerNeedLoginException()
    }
    if (httpCode >= 400) {
        val response = errorBody()?.string()
        if (response != null) {
            parseServerError(httpCode, response)?.let {
                throw ServerError(it.message)
            }
        }
    }
    return body()!!
}