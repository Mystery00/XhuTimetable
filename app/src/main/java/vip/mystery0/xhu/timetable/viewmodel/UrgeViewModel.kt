package vip.mystery0.xhu.timetable.viewmodel

import android.util.Log
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import vip.mystery0.xhu.timetable.base.PagingComposeViewModel
import vip.mystery0.xhu.timetable.config.networkErrorHandler
import vip.mystery0.xhu.timetable.config.store.UserStore
import vip.mystery0.xhu.timetable.model.response.UrgeItem
import vip.mystery0.xhu.timetable.repository.UrgeRepo
import vip.mystery0.xhu.timetable.trackEvent

class UrgeViewModel : PagingComposeViewModel<Long, UrgeItem>(
    {
        UrgeRepo.getUrgeListStream()
    }
) {
    companion object {
        private const val TAG = "UrgeViewModel"
    }

    private val _urgeLoading = MutableStateFlow(false)
    val urgeLoading: StateFlow<Boolean> = _urgeLoading

    val userName = MutableStateFlow("")
    val remainCount: StateFlow<Int> = UrgeRepo.remainCountFlow

    fun init() {
        viewModelScope.launch {
            userName.value = UserStore.getMainUser()?.info?.name ?: "未登录"
            loadData(0)
        }
    }

    fun urge(urgeId: Long) {
        fun failed(message: String) {
            Log.w(TAG, "urge failed, $message")
            _urgeLoading.value = false
            toastMessage(message)
        }
        viewModelScope.launch(networkErrorHandler { throwable ->
            Log.w(TAG, "urge failed", throwable)
            failed(throwable.message ?: throwable.javaClass.simpleName)
        }) {
            _urgeLoading.value = true
            trackEvent("催更")
            UrgeRepo.doUrge(urgeId)
            _urgeLoading.value = false
            loadData(System.currentTimeMillis())
        }
    }
}