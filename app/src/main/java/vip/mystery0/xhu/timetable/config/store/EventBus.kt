package vip.mystery0.xhu.timetable.config.store

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import vip.mystery0.xhu.timetable.model.event.EventType

object EventBus {
    private val flow = MutableSharedFlow<EventType>()

    fun flow(): SharedFlow<EventType> = flow

    suspend fun post(eventType: EventType) {
        flow.emit(eventType)
    }

    suspend fun tryPost(eventType: EventType) {
        flow.tryEmit(eventType)
    }

    suspend fun subscribe(lifecycle: Lifecycle, block: (EventType) -> Unit) {
        lifecycle.repeatOnLifecycle(Lifecycle.State.CREATED) {
            flow.collect(block)
        }
    }
}