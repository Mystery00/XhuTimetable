package vip.mystery0.xhu.timetable.viewmodel

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import vip.mystery0.xhu.timetable.base.ComposeViewModel
import vip.mystery0.xhu.timetable.config.DataHolder

class SplashImageViewModel : ComposeViewModel(), KoinComponent {
    companion object {
        private const val TAG = "SplashImageViewModel"
    }

    private val _timerState = MutableStateFlow(-1)
    val timerState: StateFlow<Int> = _timerState

    init {
        viewModelScope.launch {
            _timerState.emit(DataHolder.splashShowTime)
            (DataHolder.splashShowTime - 1 downTo 0).forEach {
                delay(1000L)
                _timerState.emit(it)
            }
        }
    }

    fun skip() {
        viewModelScope.launch {
            _timerState.value = 0
        }
    }
}