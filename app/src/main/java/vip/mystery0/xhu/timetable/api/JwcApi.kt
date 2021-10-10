package vip.mystery0.xhu.timetable.api

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query
import vip.mystery0.xhu.timetable.model.response.CourseResponse
import vip.mystery0.xhu.timetable.model.response.ExamResponse
import vip.mystery0.xhu.timetable.model.response.ScoreResponse

interface JwcApi {
    @GET("/api/rest/xhu-timetable/server/jwc/course")
    suspend fun courseList(
        @Header("token") token: String,
        @Query("year") year: String,
        @Query("term") term: Int,
    ): Response<List<CourseResponse>>

    @GET("/api/rest/xhu-timetable/server/jwc/exam")
    suspend fun examList(@Header("token") token: String): Response<ExamResponse>

    @GET("/api/rest/xhu-timetable/server/jwc/score")
    suspend fun scoreList(
        @Header("token") token: String,
        @Query("year") year: String,
        @Query("term") term: Int,
    ): Response<ScoreResponse>
}