package vip.mystery0.xhu.timetable.api

import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.Header
import vip.mystery0.xhu.timetable.model.response.BackgroundResponse

interface BackgroundApi {
    @GET("api/rest/external/background/list")
    suspend fun selectList(
        @Header("sessionToken") token: String,
    ): List<BackgroundResponse>
}