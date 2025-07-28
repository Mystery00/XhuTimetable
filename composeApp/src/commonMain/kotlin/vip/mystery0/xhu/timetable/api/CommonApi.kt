package vip.mystery0.xhu.timetable.api

import de.jensklingenberg.ktorfit.http.Body
import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.POST
import de.jensklingenberg.ktorfit.http.Query
import io.ktor.client.statement.HttpResponse
import vip.mystery0.xhu.timetable.model.request.ClientInitRequest
import vip.mystery0.xhu.timetable.model.response.ClientInitResponse
import vip.mystery0.xhu.timetable.model.response.TeamMemberResponse
import vip.mystery0.xhu.timetable.model.response.VersionUrl

interface CommonApi {
    @POST("api/rest/external/common/client/init")
    suspend fun clientInit(
        @Body request: ClientInitRequest
    ): ClientInitResponse

    @GET("api/rest/external/common/version")
    suspend fun checkVersion(
        @Query("checkBeta") checkBeta: Boolean,
        @Query("alwaysShowVersion") alwaysShowVersion: Boolean,
    ): HttpResponse

    @GET("api/rest/external/common/version/url")
    suspend fun getVersionUrl(@Query("versionId") versionId: Long): VersionUrl

    @GET("api/rest/external/common/team")
    suspend fun getTeamMemberList(): List<TeamMemberResponse>
}