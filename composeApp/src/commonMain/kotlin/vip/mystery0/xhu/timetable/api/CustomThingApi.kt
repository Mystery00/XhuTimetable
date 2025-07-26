package vip.mystery0.xhu.timetable.api

import de.jensklingenberg.ktorfit.http.Body
import de.jensklingenberg.ktorfit.http.DELETE
import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.Header
import de.jensklingenberg.ktorfit.http.POST
import de.jensklingenberg.ktorfit.http.PUT
import de.jensklingenberg.ktorfit.http.Query
import vip.mystery0.xhu.timetable.model.request.CustomThingRequest
import vip.mystery0.xhu.timetable.model.response.CustomThingResponse
import vip.mystery0.xhu.timetable.model.transfer.PageResult

interface CustomThingApi {
    @GET("api/rest/external/thing/custom/list")
    suspend fun customThingList(
        @Header("sessionToken") token: String,
        @Query("index") index: Int,
        @Query("size") size: Int,
    ): PageResult<CustomThingResponse>

    @POST("api/rest/external/thing/custom")
    suspend fun createCustomThing(
        @Header("sessionToken") token: String,
        @Body customThingRequest: CustomThingRequest
    ): Boolean

    @PUT("api/rest/external/thing/custom")
    suspend fun updateCustomThing(
        @Header("sessionToken") token: String,
        @Query("id") id: Long,
        @Body customThingRequest: CustomThingRequest
    ): Boolean

    @DELETE("api/rest/external/thing/custom")
    suspend fun deleteCustomThing(
        @Header("sessionToken") token: String,
        @Query("id") id: Long,
    ): Boolean
}