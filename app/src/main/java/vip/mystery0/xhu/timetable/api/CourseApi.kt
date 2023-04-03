package vip.mystery0.xhu.timetable.api

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query
import vip.mystery0.xhu.timetable.model.response.CourseResponse

interface CourseApi {
    @GET("/api/rest/external/course/list")
    suspend fun courseList(
        @Header("sessionToken") token: String,
        @Query("year") year: Int,
        @Query("term") term: Int,
        @Query("showCustomCourse") showCustomCourse: Boolean,
        @Query("autoCache") autoCache: Boolean,
    ): Response<CourseResponse>
}