package vip.mystery0.xhu.timetable.api

import retrofit2.Response
import retrofit2.http.*
import vip.mystery0.xhu.timetable.config.ServerError
import vip.mystery0.xhu.timetable.config.interceptor.ServerNeedLoginException
import vip.mystery0.xhu.timetable.config.parseServerError
import vip.mystery0.xhu.timetable.model.request.*
import vip.mystery0.xhu.timetable.model.response.*

interface ServerApi {
    @GET("/api/rest/xhu-timetable/common/version/url")
    suspend fun versionUrl(
        @Query("versionId") versionId: Long,
        @Query("betaVersion") betaVersion: Boolean,
    ): VersionUrl

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