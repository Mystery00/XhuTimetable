package vip.mystery0.xhu.timetable.api

import retrofit2.http.GET
import vip.mystery0.xhu.timetable.model.response.PoemsSentence
import vip.mystery0.xhu.timetable.model.response.PoemsToken

interface PoemsApi {
    @GET("/token")
    suspend fun getToken(): PoemsToken

    @GET("/one.json")
    suspend fun getSentence(): PoemsSentence
}