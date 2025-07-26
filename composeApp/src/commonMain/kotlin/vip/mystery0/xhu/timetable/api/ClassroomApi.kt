package vip.mystery0.xhu.timetable.api

import de.jensklingenberg.ktorfit.http.Body
import de.jensklingenberg.ktorfit.http.Header
import de.jensklingenberg.ktorfit.http.POST
import de.jensklingenberg.ktorfit.http.Query
import vip.mystery0.xhu.timetable.model.request.ClassroomRequest
import vip.mystery0.xhu.timetable.model.response.ClassroomResponse
import vip.mystery0.xhu.timetable.model.transfer.PageResult

interface ClassroomApi {
    @POST("api/rest/external/room/free/list")
    suspend fun getFreeList(
        @Header("sessionToken") token: String,
        @Body request: ClassroomRequest,
        @Query("index") index: Int,
        @Query("size") size: Int,
    ): PageResult<ClassroomResponse>
}