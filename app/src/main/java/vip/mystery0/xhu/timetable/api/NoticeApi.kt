package vip.mystery0.xhu.timetable.api

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query
import vip.mystery0.xhu.timetable.model.response.NoticeResponse
import vip.mystery0.xhu.timetable.model.transfer.PageResult

interface NoticeApi {
    @GET("/api/rest/external/notice/list")
    suspend fun noticeList(
        @Header("sessionToken") token: String,
        @Query("platform") platform: String = "ANDROID",
        @Query("index") index: Int,
        @Query("size") size: Int,
    ): Response<PageResult<NoticeResponse>>

    @GET("/api/rest/external/notice/check")
    suspend fun checkNotice(
        @Header("sessionToken") token: String,
        @Query("lastNoticeId") lastNoticeId: Int,
        @Query("platform") platform: String = "ANDROID",
    ): Response<Boolean>
}