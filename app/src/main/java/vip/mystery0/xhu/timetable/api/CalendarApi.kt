package vip.mystery0.xhu.timetable.api

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query
import vip.mystery0.xhu.timetable.model.response.CalendarEventResponse
import vip.mystery0.xhu.timetable.model.response.SchoolCalendarResponse

interface CalendarApi {
    @GET("/api/rest/external/calendar/school/list")
    suspend fun selectAllBackground(
        @Header("sessionToken") token: String,
    ): Response<List<SchoolCalendarResponse>>

    @GET("/api/rest/external/calendar/export")
    suspend fun exportCalendarEventList(
        @Header("sessionToken") token: String,
        @Query("year") year: Int,
        @Query("term") term: Int,
        @Query("includeCustomCourse") includeCustomCourse: Boolean,
        @Query("includeCustomThing") includeCustomThing: Boolean,
    ): Response<List<CalendarEventResponse>>
}