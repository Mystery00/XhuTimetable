package vip.mystery0.xhu.timetable.api

import retrofit2.Response
import retrofit2.http.*
import vip.mystery0.xhu.timetable.config.ServerError
import vip.mystery0.xhu.timetable.config.interceptor.ServerNeedLoginException
import vip.mystery0.xhu.timetable.config.parseServerError
import vip.mystery0.xhu.timetable.model.request.*
import vip.mystery0.xhu.timetable.model.response.*

interface ServerApi {
    @GET("/api/rest/external/calendar/export")
    suspend fun getCalendarEventList(
        @Header("sessionToken") token: String,
        @Query("year") year: Int,
        @Query("term") term: Int,
        @Query("includeCustomCourse") includeCustomCourse: Boolean,
        @Query("includeCustomThing") includeCustomThing: Boolean,
    ): Response<List<CalendarEventResponse>>

    @GET("/api/rest/xhu-timetable/health/detect")
    suspend fun serverDetect(): Response<List<DetectResponse>>
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