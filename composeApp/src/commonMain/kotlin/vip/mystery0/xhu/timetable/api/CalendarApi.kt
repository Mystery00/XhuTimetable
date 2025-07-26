package vip.mystery0.xhu.timetable.api

import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.Header
import de.jensklingenberg.ktorfit.http.Query
import vip.mystery0.xhu.timetable.model.response.CalendarEventResponse
import vip.mystery0.xhu.timetable.model.response.SchoolCalendarResponse

interface CalendarApi {
    @GET("api/rest/external/calendar/school/list")
    suspend fun selectAllBackground(
        @Header("sessionToken") token: String,
    ): List<SchoolCalendarResponse>

    @GET("api/rest/external/calendar/export")
    suspend fun exportCalendarEventList(
        @Header("sessionToken") token: String,
        @Query("year") year: Int,
        @Query("term") term: Int,
        @Query("includeCustomCourse") includeCustomCourse: Boolean,
        @Query("includeCustomThing") includeCustomThing: Boolean,
    ): List<CalendarEventResponse>
}