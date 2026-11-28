package vip.mystery0.xhu.timetable.repository

import androidx.annotation.AnyThread
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import co.touchlab.kermit.Logger
import kotlinx.coroutines.flow.Flow
import org.koin.core.component.inject
import vip.mystery0.xhu.timetable.api.NoticeApi
import vip.mystery0.xhu.timetable.base.BaseDataRepo
import vip.mystery0.xhu.timetable.base.buildPageSource
import vip.mystery0.xhu.timetable.config.store.EventBus
import vip.mystery0.xhu.timetable.config.store.UserStore.withAutoLoginOnce
import vip.mystery0.xhu.timetable.config.store.getCacheStore
import vip.mystery0.xhu.timetable.config.store.setCacheStore
import vip.mystery0.xhu.timetable.model.event.EventType
import vip.mystery0.xhu.timetable.model.response.NoticeResponse
import vip.mystery0.xhu.timetable.model.transfer.PageResult
import vip.mystery0.xhu.timetable.platform

object NoticeRepo : BaseDataRepo {
    private val logger = Logger.withTag("NoticeRepo")
    private val noticeApi: NoticeApi by inject()

    private val globalPagingConfig: PagingConfig
        @AnyThread get() = PagingConfig(
            pageSize = 10,
            initialLoadSize = 10,
        )

    fun getNoticeListStream(): Flow<PagingData<NoticeResponse>> = Pager(
        config = globalPagingConfig,
        pagingSourceFactory = {
            buildPageSource { index, size -> getNoticePage(index, size) }
        }
    ).flow

    suspend fun getNoticePage(index: Int, size: Int):
        PageResult<NoticeResponse> {
        checkForceLoadFromCloud(true)
        val result = mainUser().withAutoLoginOnce {
            noticeApi.noticeList(it, platform = platform().name, index = index, size = size)
        }
        result.items.maxOfOrNull { it.noticeId }?.let {
            val last = getCacheStore { lastNoticeId }
            setCacheStore { lastNoticeId = maxOf(last, it) }
        }
        EventBus.post(EventType.UPDATE_NOTICE_CHECK)
        return result
    }

    suspend fun checkNotice(): Boolean {
        if (!isOnline) {
            return false
        }

        val lastNoticeId = getCacheStore { lastNoticeId }
        val result = mainUser().withAutoLoginOnce {
            noticeApi.checkNotice(it, lastNoticeId, platform = platform().name)
        }
        logger.d("checkNotice: lastNoticeId=$lastNoticeId, platform=${platform().name} -> $result")
        return result
    }
}