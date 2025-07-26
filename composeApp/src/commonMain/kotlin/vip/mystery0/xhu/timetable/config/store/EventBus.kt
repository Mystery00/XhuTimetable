package vip.mystery0.xhu.timetable.config.store

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import vip.mystery0.xhu.timetable.model.event.EventType

object EventBus {
    private val _flow = MutableSharedFlow<SingleEvent<EventType>>(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val flow: SharedFlow<SingleEvent<EventType>> = _flow

    suspend fun post(eventType: EventType) {
        _flow.emit(SingleEvent(eventType))
    }
}

/**
 * 用于包装事件内容，确保内容只被消费一次。
 * @param T 事件内容的类型
 */
data class SingleEvent<out T>(private val content: T) {
    private var hasBeenHandled = false

    /**
     * 如果事件未被处理，则返回内容，并将其标记为已处理。
     * 如果事件已被处理，则返回 null。
     */
    fun getContentIfNotHandled(): T? {
        return if (hasBeenHandled) {
            null
        } else {
            hasBeenHandled = true
            content
        }
    }

    /**
     * 查看事件内容，但不将其标记为已处理。
     */
    fun peekContent(): T = content
}