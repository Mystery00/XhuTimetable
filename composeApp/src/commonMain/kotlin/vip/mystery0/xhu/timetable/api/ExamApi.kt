package vip.mystery0.xhu.timetable.api

import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.Header
import de.jensklingenberg.ktorfit.http.Query
import vip.mystery0.xhu.timetable.model.response.ExamResponse
import vip.mystery0.xhu.timetable.model.transfer.PageResult

interface ExamApi {
    @GET("api/rest/external/exam/list")
    suspend fun examList(
        @Header("sessionToken") token: String,
        @Query("year") year: Int,
        @Query("term") term: Int,
        @Query("index") index: Int,
        @Query("size") size: Int,
    ): PageResult<ExamResponse>

    @GET("api/rest/external/exam/tomorrow")
    suspend fun tomorrowExamList(
        @Header("sessionToken") token: String,
        @Query("year") year: Int,
        @Query("term") term: Int,
    ): List<ExamResponse>
}