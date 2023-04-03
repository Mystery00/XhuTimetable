package vip.mystery0.xhu.timetable.api

import retrofit2.http.Body
import retrofit2.http.POST
import vip.mystery0.xhu.timetable.model.request.ClientInitRequest
import vip.mystery0.xhu.timetable.model.response.ClientInitResponse

interface CommonApi {
    @POST("/api/rest/external/common/client/init")
    suspend fun clientInit(@Body request: ClientInitRequest): ClientInitResponse
}