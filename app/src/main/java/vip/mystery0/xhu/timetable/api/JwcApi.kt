package vip.mystery0.xhu.timetable.api

import retrofit2.Response
import retrofit2.http.*
import vip.mystery0.xhu.timetable.model.request.CourseRoomRequest
import vip.mystery0.xhu.timetable.model.response.*

interface JwcApi {
    @GET("/api/rest/xhu-timetable/server/jwc/course")
    suspend fun courseList(
        @Header("sessionToken") token: String,
        @Query("year") year: String,
        @Query("term") term: Int,
        @Query("showCustomCourse") showCustomCourse: Boolean,
        @Query("autoCache") autoCache: Boolean,
    ): Response<List<OldCourseResponse>>

    @GET("/api/rest/xhu-timetable/server/jwc/exam")
    suspend fun examList(
        @Header("sessionToken") token: String,
        @Query("year") year: String,
        @Query("term") term: Int,
    ): Response<ExamResponse>

    @GET("/api/rest/xhu-timetable/server/jwc/score")
    suspend fun scoreList(
        @Header("sessionToken") token: String,
        @Query("year") year: String,
        @Query("term") term: Int,
    ): Response<ScoreResponse>

    @GET("/api/rest/xhu-timetable/server/jwc/exp/score")
    suspend fun expScoreList(
        @Header("sessionToken") token: String,
        @Query("year") year: String,
        @Query("term") term: Int,
    ): Response<List<ExpScoreResponse>>

    @POST("/api/rest/xhu-timetable/server/jwc/course/room/free")
    suspend fun courseRoomList(
        @Header("sessionToken") token: String,
        @Body request: CourseRoomRequest,
    ): Response<List<CourseRoomResponse>>
}