package vip.mystery0.xhu.timetable.model.ws

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter

private val systemMessageAdapter = SystemMessageAdapter()
private val typeOption = JsonReader.Options.of("type")

class MessageAdapter : JsonAdapter<BaseMessage>() {
    override fun fromJson(reader: JsonReader): BaseMessage {
        val newReader = reader.peekJson()
        newReader.beginObject()
        var type: Int = WsMessageType.TEXT.type
        while (newReader.hasNext()) {
            if (newReader.selectName(typeOption) == 0) {
                type = newReader.nextInt()
            }
            newReader.skipName()
            newReader.skipValue()
        }
        newReader.endObject()
        return if (type == WsMessageType.TEXT.type) {
            WsMessageType.TEXT.adapter.fromJson(reader) as TextMessage
        } else {
            systemMessageAdapter.fromJson(reader) as SystemMessage
        }
    }

    override fun toJson(writer: JsonWriter, m: BaseMessage?) {
    }
}