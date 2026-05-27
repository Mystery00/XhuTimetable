package vip.mystery0.xhu.timetable.api

import de.jensklingenberg.ktorfit.http.Body
import de.jensklingenberg.ktorfit.http.DELETE
import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.Header
import de.jensklingenberg.ktorfit.http.POST
import de.jensklingenberg.ktorfit.http.Query
import vip.mystery0.xhu.timetable.model.request.AutoScoreStartRequest
import vip.mystery0.xhu.timetable.model.request.ClientTestRequest
import vip.mystery0.xhu.timetable.model.response.AutoScoreStartResponse
import vip.mystery0.xhu.timetable.model.response.AutoScoreStatusResponse

interface JobApi {
    @POST("api/rest/external/job/start")
    suspend fun startAutoScoreJob(
        @Header("sessionToken") token: String,
        @Query("job") job: String = "auto-score",
        @Body request: AutoScoreStartRequest,
    ): AutoScoreStartResponse

    @POST("api/rest/external/job/start")
    suspend fun startClientTestJob(
        @Header("sessionToken") token: String,
        @Query("job") job: String = "client-test",
        @Body request: ClientTestRequest,
    )

    @DELETE("api/rest/external/job/stop")
    suspend fun stopAutoScoreJob(
        @Header("sessionToken") token: String,
        @Query("job") job: String = "auto-score",
    )

    @GET("api/rest/external/job/status")
    suspend fun getAutoScoreJobStatus(
        @Header("sessionToken") token: String,
        @Query("job") job: String = "auto-score",
    ): AutoScoreStatusResponse
}
