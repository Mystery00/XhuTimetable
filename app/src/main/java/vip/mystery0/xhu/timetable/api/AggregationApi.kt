package vip.mystery0.xhu.timetable.api

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query
import vip.mystery0.xhu.timetable.model.response.AggregationMainPageResponse
import vip.mystery0.xhu.timetable.model.response.CalendarWeekResponse

interface AggregationApi {
    @GET("/api/rest/external/aggregation/page/main")
    suspend fun pageMain(
        @Header("sessionToken") token: String,
        @Query("year") year: Int,
        @Query("term") term: Int,
        @Query("showCustomCourse") showCustomCourse: Boolean,
        @Query("showCustomThing") showCustomThing: Boolean,
    ): Response<AggregationMainPageResponse>

    @GET("/api/rest/external/aggregation/page/calendar")
    suspend fun pageCalendar(
        @Header("sessionToken") token: String,
        @Query("startDate") startDate: String,
        @Query("year") year: Int,
        @Query("term") term: Int,
    ): Response<List<CalendarWeekResponse>>
}