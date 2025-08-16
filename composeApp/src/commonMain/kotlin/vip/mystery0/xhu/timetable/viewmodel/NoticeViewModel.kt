package vip.mystery0.xhu.timetable.viewmodel

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import vip.mystery0.xhu.timetable.base.PagingComposeViewModel
import vip.mystery0.xhu.timetable.model.response.NoticeResponse
import vip.mystery0.xhu.timetable.repository.NoticeRepo
import kotlin.time.Clock

class NoticeViewModel : PagingComposeViewModel<String, NoticeResponse>(
    {
        NoticeRepo.getNoticeListStream()
    }
) {
    fun loadList() {
        viewModelScope.launch {
            loadData(Clock.System.now().toEpochMilliseconds().toString())
        }
    }
}