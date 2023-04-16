package vip.mystery0.xhu.timetable.api

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query
import vip.mystery0.xhu.timetable.model.response.CustomThingResponse
import vip.mystery0.xhu.timetable.model.transfer.PageResult

interface ThingApi {
    @GET("/api/rest/external/thing/custom/list")
    suspend fun thingList(
        @Header("sessionToken") token: String,
        @Query("size") size: Int,
        @Query("lastId") lastId: Long,
    ): Response<PageResult<CustomThingResponse>>
}