package vip.mystery0.xhu.timetable.repository.remote

import androidx.compose.ui.graphics.Color
import org.koin.core.component.inject
import vip.mystery0.xhu.timetable.api.ServerApi
import vip.mystery0.xhu.timetable.api.checkLogin
import vip.mystery0.xhu.timetable.config.ServerError
import vip.mystery0.xhu.timetable.config.UserStore.withAutoLogin
import vip.mystery0.xhu.timetable.config.User
import vip.mystery0.xhu.timetable.config.chinaZone
import vip.mystery0.xhu.timetable.config.runOnCpu
import vip.mystery0.xhu.timetable.model.CustomThing
import vip.mystery0.xhu.timetable.model.request.CustomThingRequest
import vip.mystery0.xhu.timetable.module.localRepo
import vip.mystery0.xhu.timetable.repository.CustomThingRepo
import java.time.Instant
import java.util.Locale

class CustomThingRemoteRepo : CustomThingRepo {
    private val serverApi: ServerApi by inject()

    private val local: CustomThingRepo by localRepo()

    override suspend fun getCustomThingList(
        user: User,
        year: String,
        term: Int
    ): List<CustomThing> = runOnCpu {
        val response = user.withAutoLogin {
            serverApi.customThingList(it, year, term).checkLogin()
        }
        val customThingList = response.first.map {
            val startTime = Instant.ofEpochMilli(it.startTime).atZone(chinaZone).toLocalDateTime()
            val endTime = Instant.ofEpochMilli(it.endTime).atZone(chinaZone).toLocalDateTime()
            val parseColor = android.graphics.Color.parseColor(it.color)
            CustomThing(
                it.thingId,
                it.title,
                it.location,
                it.allDay,
                startTime,
                endTime,
                it.remark,
                it.color,
                Color(parseColor),
                it.extraData,
            )
        }
        local.saveCustomThingList(year, term, user.studentId, customThingList)
        customThingList
    }

    override suspend fun createCustomThing(
        user: User,
        year: String,
        term: Int,
        customThing: CustomThing
    ) {
        val color = toColorString(customThing.color)
        val request = CustomThingRequest.valueOf(customThing, color, year, term)
        val response = user.withAutoLogin {
            serverApi.createCustomThing(it, request).checkLogin()
        }
        if (!response.first) {
            throw ServerError("创建自定义事项失败")
        }
    }

    override suspend fun updateCustomThing(
        user: User,
        year: String,
        term: Int,
        customThing: CustomThing
    ) {
        val color = toColorString(customThing.color)
        val request = CustomThingRequest.valueOf(customThing, color, year, term)
        val response = user.withAutoLogin {
            serverApi.updateCustomThing(it, customThing.thingId, request).checkLogin()
        }
        if (!response.first) {
            throw ServerError("更新自定义事项失败")
        }
    }

    override suspend fun deleteCustomThing(user: User, year: String, term: Int, thingId: Long) {
        val response = user.withAutoLogin {
            serverApi.deleteCustomThing(it, thingId).checkLogin()
        }
        if (!response.first) {
            throw ServerError("删除自定义事项失败")
        }
    }
}

private fun toColorString(
    color: Color,
    locale: Locale = Locale.CHINA
): String {
    val convert = android.graphics.Color.valueOf(color.red, color.green, color.blue)
    return "#${Integer.toHexString(convert.toArgb()).uppercase(locale)}"
}