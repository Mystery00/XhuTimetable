package vip.mystery0.xhu.timetable.model.ws

import com.squareup.moshi.JsonAdapter
import vip.mystery0.xhu.timetable.module.moshiAdapter

interface BaseMessage

enum class WsMessageType(
    val type: Int,
    val adapter: JsonAdapter<*>,
) {
    TEXT(0, moshiAdapter<TextMessage>()),
    ADMIN_STATUS(1, moshiAdapter<AdminStatus>()),
}