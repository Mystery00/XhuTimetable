package vip.mystery0.xhu.timetable.viewmodel

import android.app.AlarmManager
import android.util.Log
import androidx.lifecycle.viewModelScope
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import vip.mystery0.xhu.timetable.base.ComposeViewModel
import vip.mystery0.xhu.timetable.config.SessionManager
import vip.mystery0.xhu.timetable.config.networkErrorHandler
import vip.mystery0.xhu.timetable.config.store.UserStore
import vip.mystery0.xhu.timetable.config.store.getCacheStore
import vip.mystery0.xhu.timetable.config.store.getConfigStore
import vip.mystery0.xhu.timetable.doClear
import vip.mystery0.xhu.timetable.externalPictureDir
import vip.mystery0.xhu.timetable.repository.StartRepo
import vip.mystery0.xhu.timetable.utils.md5
import vip.mystery0.xhu.timetable.utils.sha1
import vip.mystery0.xhu.timetable.utils.sha256
import vip.mystery0.xhu.timetable.work.DownloadSplashWork
import java.io.File
import java.time.Instant

class StarterViewModel : ComposeViewModel(), KoinComponent {
    companion object {
        private const val TAG = "StarterViewModel"
    }

    private val workManager: WorkManager by inject()
    private val alarmManager: AlarmManager by inject()

    private val _readyState = MutableStateFlow(ReadyState(loading = true))
    val readyState: StateFlow<ReadyState> = _readyState

    private val _isLoginState = MutableStateFlow(false)
    val isLoginState: StateFlow<Boolean> = _isLoginState

    init {
        viewModelScope.launch(networkErrorHandler { throwable ->
            Log.w(TAG, "init failed", throwable)
            _readyState.value =
                ReadyState(errorMessage = throwable.message ?: throwable.javaClass.simpleName)
        }) {
            _isLoginState.value = UserStore.isLogin()
            doClear()
            SessionManager.readFromCache()
//            setTrigger(workManager)
//            setAlarmTrigger(alarmManager)
//            initPullWork()
            StartRepo.init()
            val hideTime = getConfigStore { hideSplashBefore }
            if (Instant.now().isBefore(hideTime)) {
                //已经设置了隐藏时间，且当前时间还未到达隐藏时间
                _readyState.emit(ReadyState())
                return@launch
            }
//                var version = response.latestVersion
//                if (getConfig { ignoreVersionList }.contains("${version?.versionName}-${version?.versionCode}")) {
//                    version = null
//                }
//                DataHolder.version = version
//                DataHolder.mainUserName = SessionManager.mainUserOrNull()?.info?.name ?: "未登录"
            val dir = File(externalPictureDir, "splash")
            val now = Instant.now()
            val splashList = getCacheStore { splashList }
                .filter { now >= it.startShowTime && now <= it.endShowTime }
                .map {
                    val extension = it.imageUrl.substringAfterLast(".")
                    val name = "${it.splashId.toString().sha1()}-${it.imageUrl.md5()}"
                    File(
                        dir,
                        "${name.sha256()}.${extension}"
                    ) to it
                }
                .filter { it.first.exists() }
            if (splashList.isNotEmpty()) {
                workManager.enqueue(
                    OneTimeWorkRequestBuilder<DownloadSplashWork>()
                        .build()
                )
            }
            val splash = splashList.randomOrNull()
            if (splash == null) {
                _readyState.emit(ReadyState())
                return@launch
            }
            _readyState.emit(
                ReadyState(
                    loading = false,
                    splashFile = splash.first,
                    splashId = splash.second.splashId,
                )
            )
        }
    }
}

data class ReadyState(
    val loading: Boolean = false,
    val splashFile: File? = null,
    val splashId: Long? = null,
    val errorMessage: String = "",
)