package vip.mystery0.xhu.timetable.repository

import androidx.annotation.AnyThread
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
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

object NoticeRepo : BaseDataRepo {
    private val noticeApi: NoticeApi by inject()

    private val globalPagingConfig: PagingConfig
        @AnyThread get() = PagingConfig(
            pageSize = 10,
            initialLoadSize = 10,
        )

    fun getNoticeListStream(): Flow<PagingData<NoticeResponse>> = Pager(
        config = globalPagingConfig,
        pagingSourceFactory = {
            buildPageSource { index, size ->
                checkForceLoadFromCloud(true)

                val result = mainUser().withAutoLoginOnce {
                    noticeApi.noticeList(it, index = index, size = size)
                }
                result.items.maxOfOrNull { it.noticeId }?.let {
                    val last = getCacheStore { lastNoticeId }
                    setCacheStore { lastNoticeId = maxOf(last, it) }
                }
                EventBus.post(EventType.UPDATE_NOTICE_CHECK)

                result
            }
        }
    ).flow

    suspend fun checkNotice(): Boolean {
        if (!isOnline) {
            return false
        }

        val lastNoticeId = getCacheStore { lastNoticeId }
        return mainUser().withAutoLoginOnce {
            noticeApi.checkNotice(it, lastNoticeId)
        }
    }
}