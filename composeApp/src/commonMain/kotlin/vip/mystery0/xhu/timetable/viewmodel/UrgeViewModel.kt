package vip.mystery0.xhu.timetable.viewmodel

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import vip.mystery0.xhu.timetable.base.PagingComposeViewModel
import vip.mystery0.xhu.timetable.config.coroutine.safeLaunch
import vip.mystery0.xhu.timetable.config.networkErrorHandler
import vip.mystery0.xhu.timetable.config.store.UserStore
import vip.mystery0.xhu.timetable.config.trackEvent
import vip.mystery0.xhu.timetable.model.response.UrgeItem
import vip.mystery0.xhu.timetable.module.desc
import vip.mystery0.xhu.timetable.repository.UrgeRepo
import kotlin.time.Clock

class UrgeViewModel : PagingComposeViewModel<Long, UrgeItem>(
    {
        UrgeRepo.getUrgeListStream()
    }
) {
    private val _urgeLoading = MutableStateFlow(false)
    val urgeLoading: StateFlow<Boolean> = _urgeLoading

    val userName = MutableStateFlow("")
    val remainCount: StateFlow<Int> = UrgeRepo.remainCountFlow

    fun init() {
        viewModelScope.safeLaunch {
            userName.value = UserStore.getMainUser()?.info?.name ?: "未登录"
            loadData(0)
        }
    }

    fun urge(urgeId: Long) {
        fun failed(message: String) {
            logger.w("urge failed, $message")
            _urgeLoading.value = false
            toastMessage(message)
        }
        viewModelScope.safeLaunch(onException = networkErrorHandler { throwable ->
            logger.w("urge failed", throwable)
            failed(throwable.desc())
        }) {
            _urgeLoading.value = true
            trackEvent("催更")
            UrgeRepo.doUrge(urgeId)
            _urgeLoading.value = false
            loadData(Clock.System.now().toEpochMilliseconds())
        }
    }
}