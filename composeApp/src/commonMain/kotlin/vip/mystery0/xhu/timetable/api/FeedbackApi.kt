package vip.mystery0.xhu.timetable.api

import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.Header
import de.jensklingenberg.ktorfit.http.Query
import vip.mystery0.xhu.timetable.model.response.MessageCheckResult
import vip.mystery0.xhu.timetable.model.ws.TextMessage

interface FeedbackApi {
    @GET("api/rest/access/external/pull")
    suspend fun pullMessage(
        @Header("sessionToken") sessionToken: String,
        @Query("lastId") lastId: Long,
        @Query("size") size: Int,
    ): List<TextMessage>

    @GET("api/rest/access/external/check")
    suspend fun checkMessage(
        @Header("sessionToken") sessionToken: String,
        @Query("firstId") firstId: Long,
    ): MessageCheckResult
}