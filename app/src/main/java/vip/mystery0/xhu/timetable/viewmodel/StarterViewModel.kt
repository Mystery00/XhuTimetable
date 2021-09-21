package vip.mystery0.xhu.timetable.viewmodel

import androidx.lifecycle.viewModelScope
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import vip.mystery0.xhu.timetable.base.ComposeViewModel
import vip.mystery0.xhu.timetable.config.DataHolder
import vip.mystery0.xhu.timetable.externalPictureDir
import vip.mystery0.xhu.timetable.module.repo
import vip.mystery0.xhu.timetable.repository.StartRepo
import vip.mystery0.xhu.timetable.utils.md5
import vip.mystery0.xhu.timetable.utils.sha1
import vip.mystery0.xhu.timetable.utils.sha256
import vip.mystery0.xhu.timetable.work.DownloadSplashWork
import java.io.File

class StarterViewModel : ComposeViewModel(), KoinComponent {
    private val startRepo: StartRepo by repo()
    private val workManager: WorkManager by inject()

    private val _readyState = MutableStateFlow(ReadyState(loading = true))
    val readyState: StateFlow<ReadyState> = _readyState

    init {
        viewModelScope.launch {
            val response = startRepo.init()
            DataHolder.version = response.version
            val dir = externalPictureDir
            val splashList = response.splash
                .map {
                    File(dir, "${it.splashId.toString().sha1()}-${it.imageUrl.md5()}".sha256())
                }
                .filter {
                    it.exists()
                }
            workManager.enqueue(
                OneTimeWorkRequestBuilder<DownloadSplashWork>()
                    .build()
            )
            _readyState.value = ReadyState(splash = splashList)
        }
    }
}

data class ReadyState(
    val loading: Boolean = false,
    val splash: List<File> = emptyList(),
)