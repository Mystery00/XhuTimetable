package vip.mystery0.xhu.timetable.model.ws

import android.util.Log
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import vip.mystery0.xhu.timetable.module.moshiAdapter

private const val TAG = "SystemMessage"

sealed interface SystemMessage : BaseMessage

data class AdminStatus(
    val online: Boolean,
    val onlineMessage: String,
) : SystemMessage

data class UnknownSystemMessage(
    val type: Int,
    val content: String,
) : SystemMessage

internal data class SystemMessageData(
    val type: Int,
    val content: String,
) : BaseMessage

private val systemMessageDataAdapter = moshiAdapter<SystemMessageData>()

class SystemMessageAdapter : JsonAdapter<Any>() {
    override fun fromJson(reader: JsonReader): Any? {
        val data = systemMessageDataAdapter.fromJson(reader)!!
        val messageType = WsMessageType.entries.find { it.type == data.type }
        if (messageType == null) {
            Log.i(TAG, "fromJson: unknown system message: $data")
            return UnknownSystemMessage(data.type, data.content)
        }
        return messageType.adapter.fromJson(data.content)
    }

    override fun toJson(writer: JsonWriter, a: Any?) {
    }
}
