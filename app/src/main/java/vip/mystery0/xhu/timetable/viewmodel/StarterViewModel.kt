package vip.mystery0.xhu.timetable.viewmodel

import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewModelScope
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import vip.mystery0.xhu.timetable.base.ComposeViewModel
import vip.mystery0.xhu.timetable.config.*
import vip.mystery0.xhu.timetable.doClear
import vip.mystery0.xhu.timetable.externalPictureDir
import vip.mystery0.xhu.timetable.module.getRepo
import vip.mystery0.xhu.timetable.repository.StartRepo
import vip.mystery0.xhu.timetable.setTrigger
import vip.mystery0.xhu.timetable.utils.md5
import vip.mystery0.xhu.timetable.utils.sha1
import vip.mystery0.xhu.timetable.utils.sha256
import vip.mystery0.xhu.timetable.work.DownloadSplashWork
import vip.mystery0.xhu.timetable.work.PullWork
import java.io.File
import java.time.Instant
import java.util.concurrent.TimeUnit

class StarterViewModel : ComposeViewModel(), KoinComponent {
    companion object {
        private const val TAG = "StarterViewModel"
    }

    private val startRepo: StartRepo = getRepo()
    private val workManager: WorkManager by inject()

    private val _readyState = MutableStateFlow(ReadyState(loading = true))
    val readyState: StateFlow<ReadyState> = _readyState

    init {
        viewModelScope.launch(serverExceptionHandler { throwable ->
            Log.w(TAG, "init failed", throwable)
            _readyState.value =
                ReadyState(errorMessage = throwable.message ?: throwable.javaClass.simpleName)
        }) {
            doClear()
            SessionManager.readFromCache()
            setTrigger(workManager)
            initPullWork()
            val response = startRepo.init()
            runOnCpu {
                var version = response.version
                if (getConfig { ignoreVersionList }.contains("${version?.versionName}-${version?.versionCode}")) {
                    version = null
                }
                DataHolder.version = version
                DataHolder.mainUserName = SessionManager.mainUserOrNull()?.info?.userName ?: "未登录"
                val dir = File(externalPictureDir, "splash")
                val now = Instant.now().toEpochMilli()
                val splashList = getConfig { splashList }
                    .filter { now >= it.startShowTime && now <= it.endShowTime }
                    .map {
                        val extension = it.imageUrl.substringAfterLast(".")
                        val name = "${it.splashId.toString().sha1()}-${it.imageUrl.md5()}"
                        File(
                            dir,
                            "${name.sha256()}.${extension}"
                        ) to it
                    }
                    .filter {
                        it.first.exists()
                    }
                workManager.enqueue(
                    OneTimeWorkRequestBuilder<DownloadSplashWork>()
                        .build()
                )
                val splash = splashList.randomOrNull()
                val showTime = splash?.second?.showTime ?: 0
                val backgroundColor = splash?.second?.backgroundColor?.let {
                    try {
                        Color(android.graphics.Color.parseColor(it))
                    } catch (e: Exception) {
                        null
                    }
                }
                val hideTime = getConfig { hideSplashBefore }
                if (splash != null) {
                    if (Instant.now().isAfter(hideTime)) {
                        DataHolder.splashFile = splash.first
                        DataHolder.splash = splash.second
                        DataHolder.splashShowTime = showTime
                        DataHolder.backgroundColor = backgroundColor
                    } else {
                        DataHolder.splashFile = null
                        DataHolder.splash = null
                        DataHolder.splashShowTime = 0
                        DataHolder.backgroundColor = null
                    }
                }
                _readyState.emit(ReadyState())
            }
        }
    }

    private fun initPullWork() {
        val uniqueWorkName = PullWork::class.java.name
        workManager.cancelUniqueWork(uniqueWorkName)
        workManager.enqueueUniquePeriodicWork(
            uniqueWorkName,
            ExistingPeriodicWorkPolicy.KEEP,
            PeriodicWorkRequestBuilder<PullWork>(4, TimeUnit.HOURS)
                .build()
        )
    }
}

data class ReadyState(
    val loading: Boolean = false,
    val errorMessage: String = "",
)