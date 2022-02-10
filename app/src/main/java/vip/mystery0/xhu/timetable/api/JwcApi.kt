package vip.mystery0.xhu.timetable.api

import retrofit2.Response
import retrofit2.http.*
import vip.mystery0.xhu.timetable.model.request.CourseRoomRequest
import vip.mystery0.xhu.timetable.model.response.CourseResponse
import vip.mystery0.xhu.timetable.model.response.CourseRoomResponse
import vip.mystery0.xhu.timetable.model.response.ExamResponse
import vip.mystery0.xhu.timetable.model.response.ScoreResponse

interface JwcApi {
    @GET("/api/rest/xhu-timetable/server/jwc/course")
    suspend fun courseList(
        @Header("token") token: String,
        @Query("year") year: String,
        @Query("term") term: Int,
        @Query("showCustomCourse") showCustomCourse: Boolean,
    ): Response<List<CourseResponse>>

    @GET("/api/rest/xhu-timetable/server/jwc/exam")
    suspend fun examList(
        @Header("token") token: String,
        @Query("year") year: String,
        @Query("term") term: Int,
    ): Response<ExamResponse>

    @GET("/api/rest/xhu-timetable/server/jwc/score")
    suspend fun scoreList(
        @Header("token") token: String,
        @Query("year") year: String,
        @Query("term") term: Int,
    ): Response<ScoreResponse>

    @POST("/api/rest/xhu-timetable/server/jwc/course/room/free")
    suspend fun courseRoomList(
        @Header("token") token: String,
        @Body request: CourseRoomRequest,
    ): Response<List<CourseRoomResponse>>
}