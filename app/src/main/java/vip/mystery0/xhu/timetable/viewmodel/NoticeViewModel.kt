package vip.mystery0.xhu.timetable.viewmodel

import android.util.Log
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import vip.mystery0.xhu.timetable.base.ComposeViewModel
import vip.mystery0.xhu.timetable.config.serverExceptionHandler
import vip.mystery0.xhu.timetable.model.entity.Notice
import vip.mystery0.xhu.timetable.module.localRepo
import vip.mystery0.xhu.timetable.module.repo
import vip.mystery0.xhu.timetable.repository.NoticeRepo

class NoticeViewModel : ComposeViewModel(), KoinComponent {
    companion object {
        private const val TAG = "NoticeViewModel"
    }

    private val noticeRepo: NoticeRepo by repo()
    private val local: NoticeRepo by localRepo()

    private val _noticeListState = MutableStateFlow(NoticeListState())
    val noticeListState: StateFlow<NoticeListState> = _noticeListState

    init {
        loadNoticeList()
    }

    fun loadNoticeList() {
        viewModelScope.launch(serverExceptionHandler { throwable ->
            Log.w(TAG, "load notice list failed", throwable)
            _noticeListState.value =
                NoticeListState(errorMessage = throwable.message ?: throwable.javaClass.simpleName)
        }) {
            _noticeListState.value = NoticeListState(loading = true)
            _noticeListState.value = NoticeListState(noticeList = noticeRepo.queryAllNotice())
            local.markAllAsRead()
        }
    }
}

data class NoticeListState(
    val loading: Boolean = false,
    val noticeList: List<Notice> = emptyList(),
    val errorMessage: String = "",
)