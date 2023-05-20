package vip.mystery0.xhu.timetable.api

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import vip.mystery0.xhu.timetable.model.response.BackgroundResponse

interface BackgroundApi {
    @GET("/api/rest/external/background/list")
    suspend fun selectList(
        @Header("sessionToken") token: String,
    ): Response<List<BackgroundResponse>>
}