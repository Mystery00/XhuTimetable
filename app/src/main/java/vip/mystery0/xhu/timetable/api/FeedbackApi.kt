package vip.mystery0.xhu.timetable.api

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query
import vip.mystery0.xhu.timetable.model.response.Message
import vip.mystery0.xhu.timetable.model.response.MessageCheckResult

interface FeedbackApi {
    @GET("/api/rest/access/external/pull")
    suspend fun pullMessage(
        @Header("sessionToken") sessionToken: String,
        @Query("lastId") lastId: Long,
        @Query("size") size: Int,
    ): Response<List<Message>>

    @GET("/api/rest/access/external/check")
    suspend fun checkMessage(
        @Header("sessionToken") sessionToken: String,
        @Query("firstId") firstId: Long,
    ): Response<MessageCheckResult>
}