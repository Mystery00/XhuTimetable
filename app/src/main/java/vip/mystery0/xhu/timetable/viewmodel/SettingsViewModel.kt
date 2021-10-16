package vip.mystery0.xhu.timetable.viewmodel

import android.os.SystemClock
import androidx.lifecycle.viewModelScope
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.koin.core.component.inject
import vip.mystery0.xhu.timetable.base.ComposeViewModel
import vip.mystery0.xhu.timetable.config.getConfig
import vip.mystery0.xhu.timetable.config.setConfig
import vip.mystery0.xhu.timetable.work.DownloadApkWork
import vip.mystery0.xhu.timetable.work.DownloadPatchWork

class SettingsViewModel : ComposeViewModel() {
    companion object {
        private var hits = Array(5) { 0L }
    }

    private val workManager: WorkManager by inject()

    private val _errorMessage = MutableStateFlow("")
    val errorMessage: StateFlow<String> = _errorMessage

    val debugMode = MutableStateFlow(false)

    init {
        viewModelScope.launch {
            debugMode.value = getConfig { debugMode }
        }
    }

    fun enableDebugMode() {
        viewModelScope.launch {
            setConfig { debugMode = true }
            debugMode.value = true
        }
    }

    fun disableDebugMode() {
        viewModelScope.launch {
            setConfig { debugMode = false }
            debugMode.value = false
        }
    }

    fun clickVersion(timeout: Long): Boolean {
        System.arraycopy(hits, 1, hits, 0, hits.lastIndex)
        hits[hits.lastIndex] = SystemClock.uptimeMillis()
        if (hits[0] >= SystemClock.uptimeMillis() - timeout) {
            hits = Array(5) { 0L }
            return true
        }
        return false
    }

    fun downloadApk() {
        viewModelScope.launch {
            workManager.enqueue(
                OneTimeWorkRequestBuilder<DownloadApkWork>()
                    .build()
            )
        }
    }

    fun downloadPatch() {
        viewModelScope.launch {
            workManager.enqueue(
                OneTimeWorkRequestBuilder<DownloadPatchWork>()
                    .build()
            )
        }
    }
}