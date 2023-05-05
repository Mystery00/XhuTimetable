package vip.mystery0.xhu.timetable.api

import retrofit2.Response
import retrofit2.http.*
import vip.mystery0.xhu.timetable.model.request.ClassroomRequest
import vip.mystery0.xhu.timetable.model.response.*

interface JwcApi {

    @POST("/api/rest/xhu-timetable/server/jwc/course/room/free")
    suspend fun courseRoomList(
        @Header("sessionToken") token: String,
        @Body request: ClassroomRequest,
    ): Response<List<ClassroomResponse>>
}