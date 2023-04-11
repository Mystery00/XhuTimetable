package vip.mystery0.xhu.timetable.api

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import vip.mystery0.xhu.timetable.config.store.GlobalConfigStore
import vip.mystery0.xhu.timetable.model.request.ClientInitRequest
import vip.mystery0.xhu.timetable.model.response.ClientInitResponse
import vip.mystery0.xhu.timetable.model.response.ClientVersion

interface CommonApi {
    @POST("/api/rest/external/common/client/init")
    suspend fun clientInit(@Body request: ClientInitRequest): ClientInitResponse

    @GET("/api/rest/external/common/version")
    suspend fun checkVersion(@Query("checkBeta") checkBeta: Boolean = GlobalConfigStore.versionChannel.isBeta()): ClientVersion?
}