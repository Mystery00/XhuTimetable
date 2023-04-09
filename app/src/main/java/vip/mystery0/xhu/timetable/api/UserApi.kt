package vip.mystery0.xhu.timetable.api

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import vip.mystery0.xhu.timetable.model.UserInfo
import vip.mystery0.xhu.timetable.model.request.LoginRequest
import vip.mystery0.xhu.timetable.model.response.LoginResponse
import vip.mystery0.xhu.timetable.model.response.PublicKeyResponse

interface UserApi {
    @GET("/api/rest/external/login/publicKey")
    suspend fun publicKey(): PublicKeyResponse

    @POST("/api/rest/external/login")
    suspend fun login(@Body loginRequest: LoginRequest): LoginResponse

    @GET("/api/rest/external/user/info")
    suspend fun getUserInfo(@Header("sessionToken") token: String,): UserInfo
}