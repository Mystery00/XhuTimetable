package vip.mystery0.xhu.timetable.api

import de.jensklingenberg.ktorfit.http.Body
import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.Header
import de.jensklingenberg.ktorfit.http.POST
import de.jensklingenberg.ktorfit.http.Query
import vip.mystery0.xhu.timetable.model.request.SchoolTimetableRequest
import vip.mystery0.xhu.timetable.model.response.CourseResponse
import vip.mystery0.xhu.timetable.model.response.SchoolTimetableResponse
import vip.mystery0.xhu.timetable.model.transfer.PageResult

interface CourseApi {
    @GET("api/rest/external/course/list")
    suspend fun courseList(
        @Header("sessionToken") token: String,
        @Query("year") year: Int,
        @Query("term") term: Int,
        @Query("showCustomCourse") showCustomCourse: Boolean,
    ): CourseResponse

    @GET("api/rest/external/course/selector/campus")
    suspend fun campusList(
        @Header("sessionToken") token: String,
    ): Map<String, String>

    @GET("api/rest/external/course/selector/college")
    suspend fun collegeList(
        @Header("sessionToken") token: String,
    ): Map<String, String>

    @GET("api/rest/external/course/selector/major")
    suspend fun majorList(
        @Header("sessionToken") token: String,
        @Query("collegeId") collegeId: String,
    ): Map<String, String>

    @POST("api/rest/external/course/list/all/course")
    suspend fun allCourseList(
        @Header("sessionToken") token: String,
        @Query("index") index: Int,
        @Query("size") size: Int,
        @Body request: SchoolTimetableRequest,
    ): PageResult<SchoolTimetableResponse>
}