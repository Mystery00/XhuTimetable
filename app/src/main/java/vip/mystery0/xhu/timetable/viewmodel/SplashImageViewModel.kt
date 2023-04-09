package vip.mystery0.xhu.timetable.viewmodel

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import vip.mystery0.xhu.timetable.base.ComposeViewModel
import vip.mystery0.xhu.timetable.config.store.getConfigStore
import vip.mystery0.xhu.timetable.config.store.setConfigStore
import java.io.File
import java.time.Instant
import java.time.temporal.ChronoUnit

class SplashImageViewModel : ComposeViewModel(), KoinComponent {

    private val _timerState = MutableStateFlow(-1)
    val timerState: StateFlow<Int> = _timerState

    private val _showSplashBackgroundColor = MutableStateFlow<Color?>(null)
    val showSplashBackgroundColor: StateFlow<Color?> = _showSplashBackgroundColor

    private val _showSplashLocationUrl = MutableStateFlow<String?>(null)
    val showSplashLocationUrl: StateFlow<String?> = _showSplashLocationUrl

    private val _showSplashFile = MutableStateFlow<File?>(null)
    val showSplashFile: StateFlow<File?> = _showSplashFile

    fun skip() {
        viewModelScope.launch {
            _timerState.value = 0
        }
    }

    fun hide() {
        viewModelScope.launch {
            val hideTime = Instant.now().plus(7, ChronoUnit.DAYS)
            setConfigStore { hideSplashBefore = hideTime }
        }
    }

    fun startInit(
        splashFilePath: String,
        splashId: Long,
    ) {
        viewModelScope.launch {
            val splashList = getConfigStore { splashList }
            val splash = splashList.firstOrNull() { it.splashId == splashId }
            if (splash == null) {
                _timerState.value = 0
                return@launch
            }
            val file = File(splashFilePath)
            if (!file.exists()) {
                _timerState.value = 0
                return@launch
            }
            _showSplashBackgroundColor.value = splash.backgroundColor.let {
                Color(android.graphics.Color.parseColor(it))
            }
            _showSplashLocationUrl.value = splash.locationUrl

            _timerState.value = splash.showTime
            (splash.showTime - 1 downTo 0).forEach {
                delay(1000L)
                _timerState.emit(it)
            }
        }
    }
}