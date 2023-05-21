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
import vip.mystery0.xhu.timetable.config.store.UserStore.withAutoLoginOnce
import vip.mystery0.xhu.timetable.config.store.getCacheStore
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

                mainUser().withAutoLoginOnce {
                    noticeApi.noticeList(it, index = index, size = size)
                }
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