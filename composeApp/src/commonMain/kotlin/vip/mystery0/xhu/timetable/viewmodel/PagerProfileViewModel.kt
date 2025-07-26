package vip.mystery0.xhu.timetable.viewmodel

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import vip.mystery0.xhu.timetable.base.ComposeViewModel
import vip.mystery0.xhu.timetable.config.networkErrorHandler
import vip.mystery0.xhu.timetable.config.store.Menu
import vip.mystery0.xhu.timetable.config.store.MenuStore
import vip.mystery0.xhu.timetable.config.store.UserStore
import vip.mystery0.xhu.timetable.repository.FeedbackRepo
import vip.mystery0.xhu.timetable.repository.NoticeRepo

class PagerProfileViewModel : ComposeViewModel() {
    private val _hasUnReadNotice = MutableStateFlow(false)
    val hasUnReadNotice: StateFlow<Boolean> = _hasUnReadNotice

    private val _hasUnReadFeedback = MutableStateFlow(false)
    val hasUnReadFeedback: StateFlow<Boolean> = _hasUnReadFeedback

    private val _menu = MutableStateFlow<List<List<Menu>>>(emptyList())
    val menu: StateFlow<List<List<Menu>>> = _menu

    init {
        viewModelScope.launch {
            _menu.value = MenuStore.loadAllMenu()
        }
    }

    fun checkUnReadNotice() {
        viewModelScope.launch(networkErrorHandler {
            logger.w("check unread notice failed", it)
        }) {
            if (!UserStore.isLogin()) return@launch
            _hasUnReadNotice.value = NoticeRepo.checkNotice()
        }
    }

    fun checkUnReadFeedback() {
        viewModelScope.launch(networkErrorHandler {
            logger.w("check unread feedback failed", it)
        }) {
            if (!UserStore.isLogin()) return@launch
            _hasUnReadFeedback.value = FeedbackRepo.checkUnReadFeedback()
        }
    }
}