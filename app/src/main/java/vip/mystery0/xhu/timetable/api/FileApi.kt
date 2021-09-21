package vip.mystery0.xhu.timetable.api

import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Streaming
import retrofit2.http.Url

interface FileApi {
    @Streaming
    @GET
    suspend fun download(@Url url: String): ResponseBody
}