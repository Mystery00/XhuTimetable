package vip.mystery0.xhu.timetable.api

import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.Header
import de.jensklingenberg.ktorfit.http.PUT
import de.jensklingenberg.ktorfit.http.Query
import vip.mystery0.xhu.timetable.model.response.UrgeResponse

interface UrgeApi {
    @GET("api/rest/external/urge/list")
    suspend fun getUrgeList(
        @Header("sessionToken") token: String,
        @Query("index") index: Int,
        @Query("size") size: Int,
    ): UrgeResponse

    @PUT("api/rest/external/urge")
    suspend fun urge(
        @Header("sessionToken") token: String,
        @Query("urgeId") urgeId: Long,
    )
}