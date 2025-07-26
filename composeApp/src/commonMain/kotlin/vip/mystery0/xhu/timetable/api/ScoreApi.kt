package vip.mystery0.xhu.timetable.api

import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.Header
import de.jensklingenberg.ktorfit.http.Query
import vip.mystery0.xhu.timetable.model.response.ExperimentScoreResponse
import vip.mystery0.xhu.timetable.model.response.ScoreGpaResponse
import vip.mystery0.xhu.timetable.model.response.ScoreResponse
import vip.mystery0.xhu.timetable.model.transfer.PageResult

interface ScoreApi {
    @GET("api/rest/external/score/list")
    suspend fun scoreList(
        @Header("sessionToken") token: String,
        @Query("year") year: Int,
        @Query("term") term: Int,
        @Query("index") index: Int,
        @Query("size") size: Int,
    ): PageResult<ScoreResponse>

    @GET("api/rest/external/score/experiment/list")
    suspend fun experimentScoreList(
        @Header("sessionToken") token: String,
        @Query("year") year: Int,
        @Query("term") term: Int,
    ): List<ExperimentScoreResponse>

    @GET("api/rest/external/score/gpa")
    suspend fun gpa(
        @Header("sessionToken") token: String,
        @Query("year") year: Int,
        @Query("term") term: Int,
    ): ScoreGpaResponse
}