package vip.mystery0.xhu.timetable.api

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import vip.mystery0.xhu.timetable.model.response.SchoolCalendarResponse

interface CalendarApi {
    @GET("/api/rest/external/calendar/school/list")
    suspend fun selectAllBackground(
        @Header("sessionToken") token: String,
    ): Response<List<SchoolCalendarResponse>>
}