package vip.mystery0.xhu.timetable.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Query
import vip.mystery0.xhu.timetable.model.request.CustomThingRequest
import vip.mystery0.xhu.timetable.model.response.CustomThingResponse
import vip.mystery0.xhu.timetable.model.transfer.PageResult

interface CustomThingApi {
    @GET("/api/rest/external/thing/custom/list")
    suspend fun customThingList(
        @Header("sessionToken") token: String,
        @Query("index") index: Int,
        @Query("size") size: Int,
    ): Response<PageResult<CustomThingResponse>>

    @POST("/api/rest/external/thing/custom")
    suspend fun createCustomThing(
        @Header("sessionToken") token: String,
        @Body customThingRequest: CustomThingRequest
    ): Response<Boolean>

    @PUT("/api/rest/external/thing/custom")
    suspend fun updateCustomThing(
        @Header("sessionToken") token: String,
        @Query("id") id: Long,
        @Body customThingRequest: CustomThingRequest
    ): Response<Boolean>

    @DELETE("/api/rest/external/thing/custom")
    suspend fun deleteCustomThing(
        @Header("sessionToken") token: String,
        @Query("id") id: Long,
    ): Response<Boolean>
}