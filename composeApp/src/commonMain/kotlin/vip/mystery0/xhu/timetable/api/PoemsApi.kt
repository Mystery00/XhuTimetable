package vip.mystery0.xhu.timetable.api

import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.Query
import vip.mystery0.xhu.timetable.model.response.PoemsSentence
import vip.mystery0.xhu.timetable.model.response.PoemsToken

interface PoemsApi {
    @GET("token")
    suspend fun getToken(): PoemsToken

    @GET("one.json")
    suspend fun getSentence(@Query("client") client: String = "android-sdk/1.5"): PoemsSentence
}