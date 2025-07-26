package vip.mystery0.xhu.timetable.api

import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.Query
import vip.mystery0.xhu.timetable.model.response.MenuResponse

interface MenuApi {
    @GET("api/rest/external/menu/list")
    suspend fun list(@Query("platform") platform: String = "ANDROID"): List<MenuResponse>
}