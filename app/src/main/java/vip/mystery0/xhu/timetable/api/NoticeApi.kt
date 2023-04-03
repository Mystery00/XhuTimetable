package vip.mystery0.xhu.timetable.api

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query
import vip.mystery0.xhu.timetable.model.response.NoticeResponse

interface NoticeApi {
    @GET("/api/rest/external/notice/list")
    suspend fun noticeList(
        @Header("sessionToken") token: String,
        @Query("platform") platform: String = "ANDROID",
        @Query("lastId") lastId: Long = 0,
        @Query("size") size: Int = 20,
    ): Response<List<NoticeResponse>>
}