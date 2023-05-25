package vip.mystery0.xhu.timetable.api

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PUT
import retrofit2.http.Query
import vip.mystery0.xhu.timetable.model.response.UrgeResponse

interface UrgeApi {
    @GET("/api/rest/external/urge/list")
    suspend fun getUrgeList(
        @Header("sessionToken") token: String,
        @Query("index") index: Int,
        @Query("size") size: Int,
    ): Response<UrgeResponse>

    @PUT("/api/rest/external/urge")
    suspend fun urge(
        @Header("sessionToken") token: String,
        @Query("urgeId") urgeId: Long,
    ): Response<Unit>
}