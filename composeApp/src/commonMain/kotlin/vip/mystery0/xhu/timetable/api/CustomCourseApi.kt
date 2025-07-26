package vip.mystery0.xhu.timetable.api

import de.jensklingenberg.ktorfit.http.Body
import de.jensklingenberg.ktorfit.http.DELETE
import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.Header
import de.jensklingenberg.ktorfit.http.POST
import de.jensklingenberg.ktorfit.http.PUT
import de.jensklingenberg.ktorfit.http.Query
import vip.mystery0.xhu.timetable.model.request.CustomCourseRequest
import vip.mystery0.xhu.timetable.model.response.CustomCourseResponse
import vip.mystery0.xhu.timetable.model.transfer.PageResult

interface CustomCourseApi {
    @GET("api/rest/external/course/custom/list")
    suspend fun customCourseList(
        @Header("sessionToken") token: String,
        @Query("year") year: Int,
        @Query("term") term: Int,
        @Query("index") index: Int,
        @Query("size") size: Int,
    ): PageResult<CustomCourseResponse>

    @POST("api/rest/external/course/custom")
    suspend fun createCustomCourse(
        @Header("sessionToken") token: String,
        @Body customCourseRequest: CustomCourseRequest
    ): Boolean

    @PUT("api/rest/external/course/custom")
    suspend fun updateCustomCourse(
        @Header("sessionToken") token: String,
        @Query("id") id: Long,
        @Body customCourseRequest: CustomCourseRequest
    ): Boolean

    @DELETE("api/rest/external/course/custom")
    suspend fun deleteCustomCourse(
        @Header("sessionToken") token: String,
        @Query("id") id: Long,
    ): Boolean
}