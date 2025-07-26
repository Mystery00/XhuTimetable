package vip.mystery0.xhu.timetable.api

import de.jensklingenberg.ktorfit.http.Body
import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.Header
import de.jensklingenberg.ktorfit.http.POST
import de.jensklingenberg.ktorfit.http.Query
import vip.mystery0.xhu.timetable.model.request.AutoCheckScoreRequest
import vip.mystery0.xhu.timetable.model.response.JobHistoryResponse

interface JobApi {
    @GET("api/rest/external/job/history")
    suspend fun getHistory(@Header("sessionToken") token: String): List<JobHistoryResponse>

    @POST("api/rest/external/job/test")
    suspend fun pushTest(
        @Header("sessionToken") token: String,
        @Query("registrationId") registrationId: String,
    ): Boolean

    @POST("api/rest/external/score/auto/check")
    suspend fun autoCheckScore(
        @Header("sessionToken") token: String,
        @Body request: AutoCheckScoreRequest,
    ): Boolean
}