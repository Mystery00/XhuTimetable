package vip.mystery0.xhu.timetable.api

import retrofit2.http.GET
import retrofit2.http.Query
import vip.mystery0.xhu.timetable.model.response.Message

interface FeedbackApi {
    @GET("/api/rest/access/external/pull")
    suspend fun pullMessage(
        @Query("token") token: String,
        @Query("lastId") lastId: Long,
        @Query("size") size: Int,
    ): List<Message>
}