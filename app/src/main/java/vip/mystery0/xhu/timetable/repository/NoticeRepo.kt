package vip.mystery0.xhu.timetable.repository

import androidx.annotation.AnyThread
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.PagingSource
import androidx.paging.PagingState
import kotlinx.coroutines.flow.Flow
import org.koin.core.component.inject
import vip.mystery0.xhu.timetable.api.NoticeApi
import vip.mystery0.xhu.timetable.base.BaseDataRepo
import vip.mystery0.xhu.timetable.config.store.User
import vip.mystery0.xhu.timetable.config.store.UserStore
import vip.mystery0.xhu.timetable.config.store.UserStore.withAutoLoginOnce
import vip.mystery0.xhu.timetable.config.store.getCacheStore
import vip.mystery0.xhu.timetable.config.store.setCacheStore
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
        pagingSourceFactory = { NoticePageSource(noticeApi) }
    ).flow

    suspend fun checkNotice(): Boolean {
        checkForceLoadFromCloud(true)

        val user = UserStore.mainUser()
        val lastNoticeId = getCacheStore { lastNoticeId }
        return user.withAutoLoginOnce {
            noticeApi.checkNotice(it, lastNoticeId)
        }
    }

    internal class NoticePageSource(
        private val noticeApi: NoticeApi
    ) : PagingSource<Int, NoticeResponse>() {
        override fun getRefreshKey(state: PagingState<Int, NoticeResponse>): Int? =
            state.anchorPosition?.let {
                val anchorPage = state.closestPageToPosition(it)
                anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
            }

        override suspend fun load(params: LoadParams<Int>): LoadResult<Int, NoticeResponse> {
            checkForceLoadFromCloud(true)

            val user = UserStore.mainUser()
            val index = params.key ?: 0
            val response = user.withAutoLoginOnce {
                noticeApi.noticeList(it, index = index, size = params.loadSize)
            }
            setCacheStore { lastNoticeId = response.items.maxOfOrNull { it.noticeId } ?: 0 }
            return LoadResult.Page(
                data = response.items,
                prevKey = null,
                nextKey = response.hasNext.let { if (it) index + 1 else null },
            )
        }
    }
}