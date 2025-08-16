package vip.mystery0.xhu.timetable.viewmodel

import androidx.lifecycle.viewModelScope
import vip.mystery0.xhu.timetable.base.PagingComposeViewModel
import vip.mystery0.xhu.timetable.config.coroutine.safeLaunch
import vip.mystery0.xhu.timetable.model.response.NoticeResponse
import vip.mystery0.xhu.timetable.repository.NoticeRepo
import kotlin.time.Clock

class NoticeViewModel : PagingComposeViewModel<String, NoticeResponse>(
    {
        NoticeRepo.getNoticeListStream()
    }
) {
    fun loadList() {
        viewModelScope.safeLaunch {
            loadData(Clock.System.now().toEpochMilliseconds().toString())
        }
    }
}