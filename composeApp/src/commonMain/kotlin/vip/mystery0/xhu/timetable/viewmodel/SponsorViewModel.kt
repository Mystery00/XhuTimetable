package vip.mystery0.xhu.timetable.viewmodel

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import vip.mystery0.xhu.timetable.base.ComposeViewModel
import vip.mystery0.xhu.timetable.config.coroutine.safeLaunch
import vip.mystery0.xhu.timetable.config.networkErrorHandler
import vip.mystery0.xhu.timetable.config.store.MenuStore
import vip.mystery0.xhu.timetable.model.response.SponsorResponse
import vip.mystery0.xhu.timetable.module.desc
import vip.mystery0.xhu.timetable.repository.StartRepo

class SponsorViewModel : ComposeViewModel() {
    private val _loading = MutableStateFlow(LoadingState(true))
    val loading: StateFlow<LoadingState> = _loading

    private val _refreshing = MutableStateFlow(false)
    val refreshing: StateFlow<Boolean> = _refreshing

    private val _sponsorList = MutableStateFlow<List<SponsorResponse>>(emptyList())
    val sponsorList: StateFlow<List<SponsorResponse>> = _sponsorList

    private val _sponsorLink = MutableStateFlow("")
    val sponsorLink: StateFlow<String> = _sponsorLink

    private var initialized = false

    fun init(initialLink: String = "") {
        if (initialLink.isNotBlank()) {
            _sponsorLink.value = initialLink
        }
        if (initialized) return
        initialized = true
        viewModelScope.safeLaunch {
            if (_sponsorLink.value.isBlank()) {
                _sponsorLink.value = MenuStore.findMenuByKey("sponsor")?.link.orEmpty()
            }
        }
        loadSponsorList(isRefresh = false)
    }

    fun loadSponsorList(isRefresh: Boolean = false) {
        viewModelScope.safeLaunch(onException = networkErrorHandler { throwable ->
            logger.w("load sponsor list failed", throwable)
            _refreshing.value = false
            _loading.value = LoadingState(false, throwable.desc())
        }) {
            if (isRefresh) {
                _refreshing.value = true
                _loading.value = _loading.value.copy(errorMessage = "")
            } else {
                _loading.value = LoadingState(true)
            }
            val list = StartRepo.loadSponsorList()
            _sponsorList.value = list
            _refreshing.value = false
            _loading.value = LoadingState(false)
        }
    }

    fun clearErrorMessage() {
        _loading.value = _loading.value.copy(errorMessage = "")
    }
}
