package vip.mystery0.xhu.timetable.repository

import androidx.annotation.AnyThread
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.PagingSource
import androidx.paging.PagingState
import kotlinx.coroutines.flow.Flow
import org.koin.core.component.inject
import vip.mystery0.xhu.timetable.api.CustomThingApi
import vip.mystery0.xhu.timetable.base.BaseDataRepo
import vip.mystery0.xhu.timetable.config.ServerError
import vip.mystery0.xhu.timetable.config.store.User
import vip.mystery0.xhu.timetable.config.store.UserStore.withAutoLoginOnce
import vip.mystery0.xhu.timetable.model.request.CustomThingRequest
import vip.mystery0.xhu.timetable.model.response.CustomThingResponse

object CustomThingRepo : BaseDataRepo {
    private val customThingApi: CustomThingApi by inject()

    private val globalPagingConfig: PagingConfig
        @AnyThread get() = PagingConfig(
            pageSize = 10,
            initialLoadSize = 10,
        )

    fun getCustomThingListStream(
        user: User,
    ): Flow<PagingData<CustomThingResponse>> =
        Pager(
            config = globalPagingConfig,
            pagingSourceFactory = { CustomThingPageSource(customThingApi, user) }
        ).flow

    suspend fun createCustomThing(
        user: User,
        request: CustomThingRequest,
    ) {
        val response = user.withAutoLoginOnce {
            customThingApi.createCustomThing(it, request)
        }
        if (!response) {
            throw ServerError("创建自定义事项失败")
        }
    }

    suspend fun updateCustomThing(
        user: User,
        thingId: Long,
        request: CustomThingRequest,
    ) {
        val response = user.withAutoLoginOnce {
            customThingApi.updateCustomThing(it, thingId, request)
        }
        if (!response) {
            throw ServerError("更新自定义事项失败")
        }
    }

    suspend fun deleteCustomThing(
        user: User,
        thingId: Long,
    ) {
        val response = user.withAutoLoginOnce {
            customThingApi.deleteCustomThing(it, thingId)
        }
        if (!response) {
            throw ServerError("删除自定义事项失败")
        }
    }

    internal class CustomThingPageSource(
        private val customThingApi: CustomThingApi,
        private val user: User,
    ) : PagingSource<Int, CustomThingResponse>() {
        override fun getRefreshKey(state: PagingState<Int, CustomThingResponse>): Int? =
            state.anchorPosition?.let {
                val anchorPage = state.closestPageToPosition(it)
                anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
            }

        override suspend fun load(params: LoadParams<Int>): LoadResult<Int, CustomThingResponse> {
            checkForceLoadFromCloud(true)

            val index = params.key ?: 0
            val response = user.withAutoLoginOnce {
                customThingApi.customThingList(it, index, params.loadSize)
            }
            return LoadResult.Page(
                data = response.items,
                prevKey = null,
                nextKey = response.hasNext.let { if (it) index + 1 else null },
            )
        }
    }
}