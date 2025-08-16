package vip.mystery0.xhu.timetable.viewmodel

import androidx.lifecycle.viewModelScope
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.exists
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.datetime.LocalDate
import org.koin.core.component.KoinComponent
import vip.mystery0.xhu.timetable.base.ComposeViewModel
import vip.mystery0.xhu.timetable.config.clearDownloadDir
import vip.mystery0.xhu.timetable.config.coroutine.safeLaunch
import vip.mystery0.xhu.timetable.config.externalPictureDir
import vip.mystery0.xhu.timetable.config.networkErrorHandler
import vip.mystery0.xhu.timetable.config.store.UserStore
import vip.mystery0.xhu.timetable.config.store.getCacheStore
import vip.mystery0.xhu.timetable.module.desc
import vip.mystery0.xhu.timetable.repository.StartRepo
import vip.mystery0.xhu.timetable.utils.md5
import vip.mystery0.xhu.timetable.utils.now
import vip.mystery0.xhu.timetable.utils.sha1
import vip.mystery0.xhu.timetable.utils.sha256
import kotlin.time.Clock

class StarterViewModel : ComposeViewModel(), KoinComponent {
    private val _readyState = MutableStateFlow(ReadyState(loading = true))
    val readyState: StateFlow<ReadyState> = _readyState

    private val _isLoginState = MutableStateFlow(false)
    val isLoginState: StateFlow<Boolean> = _isLoginState

    fun init() {
        viewModelScope.safeLaunch(onException = networkErrorHandler { throwable ->
            logger.w("init failed", throwable)
            _readyState.value =
                ReadyState(errorMessage = throwable.desc())
        }) {
            _isLoginState.value = UserStore.isLogin()
            clearDownloadDir()
            doPlatformInit()
            StartRepo.init()
            val hideTime = getCacheStore { hideSplashBefore }
            if (LocalDate.now() < hideTime) {
                //已经设置了隐藏时间，且当前时间还未到达隐藏时间
                _readyState.emit(ReadyState())
                return@safeLaunch
            }
            val dir = PlatformFile(externalPictureDir, "splash")
            val now = Clock.System.now()
            val splashList = getCacheStore { splashList }
                .filter { now >= it.startShowTime && now <= it.endShowTime }
                .map {
                    val extension = it.imageUrl.substringAfterLast(".")
                    val name = "${it.splashId.toString().sha1()}-${it.imageUrl.md5()}"
                    PlatformFile(
                        dir,
                        "${name.sha256()}.${extension}"
                    ) to it
                }
                .filter { it.first.exists() }
            if (splashList.isNotEmpty()) {
                doCheckDownloadSplash()
            }
            val splash = splashList.randomOrNull()
            if (splash == null) {
                _readyState.emit(ReadyState())
                return@safeLaunch
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

expect suspend fun doPlatformInit()

expect suspend fun doCheckDownloadSplash()

data class ReadyState(
    val loading: Boolean = false,
    val splashFile: PlatformFile? = null,
    val splashId: Long? = null,
    val errorMessage: String = "",
)