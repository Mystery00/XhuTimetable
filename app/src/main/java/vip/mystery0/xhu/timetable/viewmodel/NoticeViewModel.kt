package vip.mystery0.xhu.timetable.viewmodel

import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import kotlinx.coroutines.flow.Flow
import vip.mystery0.xhu.timetable.base.ComposeViewModel
import vip.mystery0.xhu.timetable.model.response.NoticeResponse
import vip.mystery0.xhu.timetable.repository.NoticeRepo

class NoticeViewModel : ComposeViewModel() {
    // 通知公告分页数据
    val pageState: Flow<PagingData<NoticeResponse>> =
        NoticeRepo.getNoticeListStream().cachedIn(viewModelScope)
}