package vip.mystery0.xhu.timetable.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query
import vip.mystery0.xhu.timetable.model.request.ClassroomRequest
import vip.mystery0.xhu.timetable.model.response.ClassroomResponse
import vip.mystery0.xhu.timetable.model.transfer.PageResult

interface ClassroomApi {
    @POST("/api/rest/external/room/free/list")
    suspend fun getFreeList(
        @Header("sessionToken") token: String,
        @Body request: ClassroomRequest,
        @Query("index") index: Int,
        @Query("size") size: Int,
    ): Response<PageResult<ClassroomResponse>>
}