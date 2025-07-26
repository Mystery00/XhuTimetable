package vip.mystery0.xhu.timetable.api

import de.jensklingenberg.ktorfit.http.Body
import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.Header
import de.jensklingenberg.ktorfit.http.POST
import de.jensklingenberg.ktorfit.http.Query
import vip.mystery0.xhu.timetable.model.request.AllCourseRequest
import vip.mystery0.xhu.timetable.model.response.AllCourseResponse
import vip.mystery0.xhu.timetable.model.response.CourseResponse
import vip.mystery0.xhu.timetable.model.transfer.PageResult

interface CourseApi {
    @GET("api/rest/external/course/list")
    suspend fun courseList(
        @Header("sessionToken") token: String,
        @Query("year") year: Int,
        @Query("term") term: Int,
        @Query("showCustomCourse") showCustomCourse: Boolean,
    ): CourseResponse

    @POST("api/rest/external/course/list/all")
    suspend fun allCourseList(
        @Header("sessionToken") token: String,
        @Query("year") year: Int,
        @Query("term") term: Int,
        @Query("index") index: Int,
        @Query("size") size: Int,
        @Body request: AllCourseRequest,
    ): PageResult<AllCourseResponse>
}