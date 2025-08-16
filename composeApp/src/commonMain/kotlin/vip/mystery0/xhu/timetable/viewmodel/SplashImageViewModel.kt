package vip.mystery0.xhu.timetable.viewmodel

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewModelScope
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.exists
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus
import org.koin.core.component.KoinComponent
import vip.mystery0.xhu.timetable.base.ComposeViewModel
import vip.mystery0.xhu.timetable.config.coroutine.safeLaunch
import vip.mystery0.xhu.timetable.config.store.getCacheStore
import vip.mystery0.xhu.timetable.config.store.setCacheStore
import vip.mystery0.xhu.timetable.utils.now
import vip.mystery0.xhu.timetable.utils.parseColorHexString

class SplashImageViewModel : ComposeViewModel(), KoinComponent {
    private val _timerState = MutableStateFlow(-1)
    val timerState: StateFlow<Int> = _timerState

    private val _showSplashBackgroundColor = MutableStateFlow<Color?>(null)
    val showSplashBackgroundColor: StateFlow<Color?> = _showSplashBackgroundColor

    private val _showSplashLocationUrl = MutableStateFlow<String?>(null)
    val showSplashLocationUrl: StateFlow<String?> = _showSplashLocationUrl

    private val _showSplashFile = MutableStateFlow<PlatformFile?>(null)
    val showSplashFile: StateFlow<PlatformFile?> = _showSplashFile

    fun skip() {
        viewModelScope.safeLaunch {
            _timerState.value = 0
        }
    }

    fun hide() {
        viewModelScope.safeLaunch {
            val hideDate = LocalDate.now().plus(7, DateTimeUnit.DAY)
            setCacheStore { hideSplashBefore = hideDate }
        }
    }

    fun startInit(
        splashFilePath: String,
        splashId: Long,
    ) {
        viewModelScope.safeLaunch {
            val splashList = getCacheStore { splashList }
            val splash = splashList.firstOrNull() { it.splashId == splashId }
            if (splash == null) {
                _timerState.value = 0
                return@safeLaunch
            }
            val file = PlatformFile(splashFilePath)
            if (!file.exists()) {
                _timerState.value = 0
                return@safeLaunch
            }
            _showSplashBackgroundColor.value = splash.backgroundColor.parseColorHexString()
            _showSplashLocationUrl.value = splash.locationUrl

            _timerState.value = splash.showTime
            (splash.showTime - 1 downTo 0).forEach {
                delay(1000L)
                _timerState.emit(it)
            }
        }
    }
}