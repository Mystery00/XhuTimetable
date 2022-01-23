package vip.mystery0.xhu.timetable.api

import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Streaming
import retrofit2.http.Tag
import retrofit2.http.Url
import vip.mystery0.xhu.timetable.config.interceptor.DownloadProgressInterceptor

interface FileApi {
    @Streaming
    @GET
    suspend fun download(
        @Url url: String,
        @Tag tag: DownloadProgressInterceptor.Tag = DownloadProgressInterceptor.emptyTag(),
    ): ResponseBody

    @Streaming
    @GET
    suspend fun downloadFile(
        @Url url: String,
    ): ResponseBody
}