package vip.mystery0.xhu.timetable.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Query
import vip.mystery0.xhu.timetable.model.request.CustomCourseRequest
import vip.mystery0.xhu.timetable.model.response.CustomCourseResponse
import vip.mystery0.xhu.timetable.model.transfer.PageResult

interface CustomCourseApi {
    @GET("/api/rest/external/course/custom/list")
    suspend fun customCourseList(
        @Header("sessionToken") token: String,
        @Query("year") year: Int,
        @Query("term") term: Int,
        @Query("index") index: Int,
        @Query("size") size: Int,
    ): Response<PageResult<CustomCourseResponse>>

    @POST("/api/rest/external/course/custom")
    suspend fun createCustomCourse(
        @Header("sessionToken") token: String,
        @Body customCourseRequest: CustomCourseRequest
    ): Response<Boolean>

    @PUT("/api/rest/external/course/custom")
    suspend fun updateCustomCourse(
        @Header("sessionToken") token: String,
        @Query("id") id: Long,
        @Body customCourseRequest: CustomCourseRequest
    ): Response<Boolean>

    @DELETE("/api/rest/external/course/custom")
    suspend fun deleteCustomCourse(
        @Header("sessionToken") token: String,
        @Query("id") id: Long,
    ): Response<Boolean>
}