package vip.mystery0.xhu.timetable.api

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query
import vip.mystery0.xhu.timetable.model.response.JobHistoryResponse

interface JobApi {
    @GET("/api/rest/external/job/history")
    suspend fun getHistory(@Header("sessionToken") token: String): Response<List<JobHistoryResponse>>

    @POST("/api/rest/external/job/test")
    suspend fun pushTest(
        @Header("sessionToken") token: String,
        @Query("registrationId") registrationId: String,
    ): Response<Boolean>
}