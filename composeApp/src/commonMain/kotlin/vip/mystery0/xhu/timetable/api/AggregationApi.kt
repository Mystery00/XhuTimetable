package vip.mystery0.xhu.timetable.api

import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.Header
import de.jensklingenberg.ktorfit.http.Query
import vip.mystery0.xhu.timetable.model.response.AggregationMainPageResponse
import vip.mystery0.xhu.timetable.model.response.CalendarWeekResponse

interface AggregationApi {
    @GET("api/rest/external/aggregation/page/main")
    suspend fun pageMain(
        @Header("sessionToken") token: String,
        @Query("year") year: Int,
        @Query("term") term: Int,
        @Query("showCustomCourse") showCustomCourse: Boolean,
        @Query("showCustomThing") showCustomThing: Boolean,
    ): AggregationMainPageResponse

    @GET("api/rest/external/aggregation/page/calendar")
    suspend fun pageCalendar(
        @Header("sessionToken") token: String,
        @Query("startDate") startDate: String,
        @Query("year") year: Int,
        @Query("term") term: Int,
    ): List<CalendarWeekResponse>
}