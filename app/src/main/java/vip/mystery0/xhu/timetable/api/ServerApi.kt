package vip.mystery0.xhu.timetable.api

import retrofit2.Response
import retrofit2.http.*
import vip.mystery0.xhu.timetable.config.GlobalConfig
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

    @GET("/api/rest/xhu-timetable/common/version")
    suspend fun getLatestVersion(@Query("betaVersion") betaVersion: Boolean = GlobalConfig.versionChannel.isBeta()): Version

    @GET("/api/rest/xhu-timetable/common/version/url")
    suspend fun versionUrl(
        @Query("versionId") versionId: Long,
        @Query("betaVersion") betaVersion: Boolean,
    ): VersionUrl

    @GET("/api/rest/external/login/publicKey")
    suspend fun publicKey(): PublicKeyResponse

    @POST("/api/rest/external/login")
    suspend fun login(@Body loginRequest: LoginRequest): LoginResponse

    @GET("/api/rest/external/user/info")
    suspend fun userInfo(@Header("sessionToken") token: String): UserInfo

    @GET("/api/rest/external/notice/list")
    suspend fun noticeList(
        @Header("sessionToken") token: String,
        @Query("platform") platform: String = "Android",
        @Query("lastId") lastId: Long = 0,
        @Query("size") size: Int = 20,
    ): Response<List<NoticeResponse>>

    @GET("/api/rest/xhu-timetable/server/schoolCalendar")
    suspend fun schoolCalendarList(@Header("sessionToken") token: String): Response<List<SchoolCalendarResponse>>

    @GET("/api/rest/xhu-timetable/server/schoolCalendarUrl")
    suspend fun schoolCalendarUrl(
        @Header("sessionToken") token: String,
        @Query("resourceId") resourceId: Long,
    ): Response<ResourceUrlResponse>

    @GET("/api/rest/xhu-timetable/server/customCourse")
    suspend fun customCourseList(
        @Header("sessionToken") token: String,
        @Query("year") year: String,
        @Query("term") term: Int,
    ): Response<List<CustomCourse>>

    @POST("/api/rest/xhu-timetable/server/customCourse")
    suspend fun createCustomCourse(
        @Header("sessionToken") token: String,
        @Body customCourseRequest: CustomCourseRequest
    ): Response<Boolean>

    @PUT("/api/rest/xhu-timetable/server/customCourse")
    suspend fun updateCustomCourse(
        @Header("sessionToken") token: String,
        @Query("id") id: Long,
        @Body customCourseRequest: CustomCourseRequest
    ): Response<Boolean>

    @DELETE("/api/rest/xhu-timetable/server/customCourse")
    suspend fun deleteCustomCourse(
        @Header("sessionToken") token: String,
        @Query("id") id: Long,
    ): Response<Boolean>

    @GET("/api/rest/xhu-timetable/server/customThing")
    suspend fun customThingList(
        @Header("sessionToken") token: String,
        @Query("year") year: String,
        @Query("term") term: Int,
    ): Response<List<CustomThingResponse>>

    @POST("/api/rest/xhu-timetable/server/customThing")
    suspend fun createCustomThing(
        @Header("sessionToken") token: String,
        @Body customThingRequest: CustomThingRequest
    ): Response<Boolean>

    @PUT("/api/rest/xhu-timetable/server/customThing")
    suspend fun updateCustomThing(
        @Header("sessionToken") token: String,
        @Query("id") id: Long,
        @Body customThingRequest: CustomThingRequest
    ): Response<Boolean>

    @DELETE("/api/rest/xhu-timetable/server/customThing")
    suspend fun deleteCustomThing(
        @Header("sessionToken") token: String,
        @Query("id") id: Long,
    ): Response<Boolean>

    @POST("/api/rest/xhu-timetable/server/allCourse")
    suspend fun selectAllCourse(
        @Header("sessionToken") token: String,
        @Body allCourseRequest: AllCourseRequest,
    ): Response<List<AllCourseResponse>>

    @GET("/api/rest/xhu-timetable/server/background")
    suspend fun selectAllBackground(
        @Header("sessionToken") token: String,
    ): Response<List<BackgroundResponse>>

    @GET("/api/rest/xhu-timetable/server/background/url")
    suspend fun getBackgroundUrl(
        @Header("sessionToken") token: String,
        @Query("resourceId") resourceId: Long,
    ): Response<ResourceUrlResponse>

    @GET("/api/rest/xhu-timetable/common/teamMember")
    suspend fun getTeamMemberList(): List<TeamMemberResponse>

    @GET("/api/rest/xhu-timetable/server/calendar")
    suspend fun getCalendarEventList(
        @Header("sessionToken") token: String,
        @Query("year") year: String,
        @Query("term") term: Int,
        @Query("includeCustomCourse") includeCustomCourse: Boolean,
        @Query("includeCustomThing") includeCustomThing: Boolean,
    ): Response<List<CalendarEventResponse>>

    @GET("/api/rest/xhu-timetable/server/urge")
    suspend fun getUrgeList(
        @Header("sessionToken") token: String,
    ): Response<UrgeResponse>

    @PUT("/api/rest/xhu-timetable/server/urge")
    suspend fun urge(
        @Header("sessionToken") token: String,
        @Query("urgeId") urgeId: Long,
    ): Response<Unit>

    @GET("/api/rest/xhu-timetable/health/detect")
    suspend fun serverDetect(): Response<List<DetectResponse>>

    @GET("/api/rest/xhu-timetable/push/fetch")
    suspend fun fetchPush(@Header("sessionToken") token: String): Response<List<PushResponse>>

    @POST("/api/rest/xhu-timetable/server/academicReport")
    suspend fun getAcademicReportList(
        @Header("sessionToken") token: String,
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