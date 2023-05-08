package vip.mystery0.xhu.timetable.api

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query
import vip.mystery0.xhu.timetable.model.response.ExperimentScoreResponse
import vip.mystery0.xhu.timetable.model.response.ScoreResponse
import vip.mystery0.xhu.timetable.model.transfer.PageResult

interface ScoreApi {
    @GET("/api/rest/external/score/list")
    suspend fun scoreList(
        @Header("sessionToken") token: String,
        @Query("year") year: Int,
        @Query("term") term: Int,
        @Query("index") index: Int,
        @Query("size") size: Int,
    ): Response<PageResult<ScoreResponse>>

    @GET("/api/rest/external/score/experiment/list")
    suspend fun experimentScoreList(
        @Header("sessionToken") token: String,
        @Query("year") year: Int,
        @Query("term") term: Int,
    ): Response<List<ExperimentScoreResponse>>
}