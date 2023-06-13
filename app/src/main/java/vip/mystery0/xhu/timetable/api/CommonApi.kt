package vip.mystery0.xhu.timetable.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import vip.mystery0.xhu.timetable.config.store.GlobalCacheStore
import vip.mystery0.xhu.timetable.config.store.GlobalConfigStore
import vip.mystery0.xhu.timetable.model.request.ClientInitRequest
import vip.mystery0.xhu.timetable.model.response.ClientInitResponse
import vip.mystery0.xhu.timetable.model.response.ClientVersion
import vip.mystery0.xhu.timetable.model.response.TeamMemberResponse
import vip.mystery0.xhu.timetable.model.response.VersionUrl

interface CommonApi {
    @POST("/api/rest/external/common/client/init")
    suspend fun clientInit(@Body request: ClientInitRequest): ClientInitResponse

    @GET("/api/rest/external/common/version")
    suspend fun checkVersion(
        @Query("checkBeta") checkBeta: Boolean,
        @Query("alwaysShowVersion") alwaysShowVersion: Boolean,
    ): Response<ClientVersion?>

    @GET("/api/rest/external/common/version/url")
    suspend fun getVersionUrl(@Query("versionId") versionId: Long): VersionUrl

    @GET("/api/rest/external/common/team")
    suspend fun getTeamMemberList(): List<TeamMemberResponse>
}