package vip.mystery0.xhu.timetable.repository.remote

import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.component.inject
import vip.mystery0.xhu.timetable.api.ServerApi
import vip.mystery0.xhu.timetable.api.ThingApi
import vip.mystery0.xhu.timetable.api.checkLogin
import vip.mystery0.xhu.timetable.config.ServerError
import vip.mystery0.xhu.timetable.config.store.UserStore.withAutoLogin
import vip.mystery0.xhu.timetable.config.store.User
import vip.mystery0.xhu.timetable.config.chinaZone
import vip.mystery0.xhu.timetable.config.runOnCpu
import vip.mystery0.xhu.timetable.config.store.UserStore.withAutoLoginOnce
import vip.mystery0.xhu.timetable.model.CustomThing
import vip.mystery0.xhu.timetable.model.request.CustomThingRequest
import vip.mystery0.xhu.timetable.model.transfer.PageResult
import vip.mystery0.xhu.timetable.module.localRepo
import vip.mystery0.xhu.timetable.repository.CustomThingRepo
import vip.mystery0.xhu.timetable.utils.asLocalDateTime
import java.time.Instant
import java.util.Locale

class CustomThingRemoteRepo : CustomThingRepo {
    private val thingApi: ThingApi by inject()

    override suspend fun fetchCustomThingList(
        user: User,
        lastId: Long,
        size: Int,
    ): PageResult<CustomThing> {
        val page = user.withAutoLoginOnce {
            thingApi.thingList(it, size, lastId)
        }
        if (page.isEmpty) {
            return page.emptyMap()
        }
        return withContext(Dispatchers.Default) {
            page.map {
                val parseColor = Color(android.graphics.Color.parseColor(it.color))
                CustomThing(
                    it.thingId,
                    it.title,
                    it.location,
                    it.allDay,
                    it.startTime.asLocalDateTime(),
                    it.endTime.asLocalDateTime(),
                    it.remark,
                    it.color,
                    parseColor,
                    it.metadata,
                )
            }
        }
    }

    override suspend fun createCustomThing(
        user: User,
        year: String,
        term: Int,
        customThing: CustomThing
    ) {
//        val color = toColorString(customThing.color)
//        val request = CustomThingRequest.valueOf(customThing, color, year, term)
//        val response = user.withAutoLogin {
//            serverApi.createCustomThing(it, request).checkLogin()
//        }
//        if (!response.first) {
//            throw ServerError("创建自定义事项失败")
//        }
    }

    override suspend fun updateCustomThing(
        user: User,
        year: String,
        term: Int,
        customThing: CustomThing
    ) {
//        val color = toColorString(customThing.color)
//        val request = CustomThingRequest.valueOf(customThing, color, year, term)
//        val response = user.withAutoLogin {
//            serverApi.updateCustomThing(it, customThing.thingId, request).checkLogin()
//        }
//        if (!response.first) {
//            throw ServerError("更新自定义事项失败")
//        }
    }

    override suspend fun deleteCustomThing(user: User, year: String, term: Int, thingId: Long) {
//        val response = user.withAutoLogin {
//            serverApi.deleteCustomThing(it, thingId).checkLogin()
//        }
//        if (!response.first) {
//            throw ServerError("删除自定义事项失败")
//        }
    }
}

private fun toColorString(
    color: Color,
    locale: Locale = Locale.CHINA
): String {
    val convert = android.graphics.Color.valueOf(color.red, color.green, color.blue)
    return "#${Integer.toHexString(convert.toArgb()).uppercase(locale)}"
}