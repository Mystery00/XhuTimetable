package vip.mystery0.xhu.timetable.api

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query
import vip.mystery0.xhu.timetable.model.response.ExamResponse
import vip.mystery0.xhu.timetable.model.transfer.PageResult

interface ExamApi {
    @GET("/api/rest/external/exam/list")
    suspend fun examList(
        @Header("sessionToken") token: String,
        @Query("year") year: Int,
        @Query("term") term: Int,
        @Query("index") index: Int,
        @Query("size") size: Int,
    ): Response<PageResult<ExamResponse>>
}