package vip.mystery0.xhu.timetable.viewmodel

import android.util.Log
import androidx.lifecycle.viewModelScope
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import vip.mystery0.xhu.timetable.base.ComposeViewModel
import vip.mystery0.xhu.timetable.config.*
import vip.mystery0.xhu.timetable.externalPictureDir
import vip.mystery0.xhu.timetable.module.repo
import vip.mystery0.xhu.timetable.repository.StartRepo
import vip.mystery0.xhu.timetable.utils.md5
import vip.mystery0.xhu.timetable.utils.sha1
import vip.mystery0.xhu.timetable.utils.sha256
import vip.mystery0.xhu.timetable.work.DownloadSplashWork
import java.io.File

class StarterViewModel : ComposeViewModel(), KoinComponent {
    companion object {
        private const val TAG = "StarterViewModel"
    }

    private val startRepo: StartRepo by repo()
    private val workManager: WorkManager by inject()

    private val _readyState = MutableStateFlow(ReadyState(loading = true))
    val readyState: StateFlow<ReadyState> = _readyState

    private val _timerState = MutableStateFlow(-1)
    val timerState: StateFlow<Int> = _timerState

    init {
        viewModelScope.launch(serverExceptionHandler { throwable ->
            Log.w(TAG, "init failed", throwable)
            _readyState.value =
                ReadyState(errorMessage = throwable.message ?: throwable.javaClass.simpleName)
        }) {
            SessionManager.readFromCache()
            val response = startRepo.init()
            runOnCpu {
                DataHolder.version = response.version
                val dir = externalPictureDir
                val splashList = getConfig { splashList }
                    .map {
                        File(
                            dir,
                            "${it.splashId.toString().sha1()}-${it.imageUrl.md5()}".sha256()
                        ) to it.showTime
                    }
                    .filter {
                        it.first.exists()
                    }
                workManager.enqueue(
                    OneTimeWorkRequestBuilder<DownloadSplashWork>()
                        .build()
                )
                val splash = splashList.randomOrNull()
                val showTime = splash?.second ?: 0
                _timerState.emit(showTime)
                _readyState.emit(ReadyState(splash = splash?.first))
                (showTime - 1 downTo 0).forEach {
                    delay(1000L)
                    _timerState.emit(it)
                }
            }
        }
    }

    fun skip() {
        viewModelScope.launch {
            _timerState.value = 0
        }
    }
}

data class ReadyState(
    val loading: Boolean = false,
    val splash: File? = null,
    val errorMessage: String = "",
)