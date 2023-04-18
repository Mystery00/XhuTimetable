package vip.mystery0.xhu.timetable.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query
import vip.mystery0.xhu.timetable.model.request.AllCourseRequest
import vip.mystery0.xhu.timetable.model.response.AllCourseResponse
import vip.mystery0.xhu.timetable.model.response.CourseResponse
import vip.mystery0.xhu.timetable.model.transfer.PageResult

interface CourseApi {
    @GET("/api/rest/external/course/list")
    suspend fun courseList(
        @Header("sessionToken") token: String,
        @Query("year") year: Int,
        @Query("term") term: Int,
        @Query("showCustomCourse") showCustomCourse: Boolean,
    ): Response<CourseResponse>

    @POST("/api/rest/external/course/list/all")
    suspend fun allCourseList(
        @Header("sessionToken") token: String,
        @Query("year") year: Int,
        @Query("term") term: Int,
        @Query("index") index: Int,
        @Query("size") size: Int,
        @Body request: AllCourseRequest,
    ): Response<PageResult<AllCourseResponse>>
}