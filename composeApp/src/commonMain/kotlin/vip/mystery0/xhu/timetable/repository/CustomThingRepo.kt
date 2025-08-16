package vip.mystery0.xhu.timetable.repository

import androidx.annotation.AnyThread
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import org.koin.core.component.inject
import vip.mystery0.xhu.timetable.api.CustomThingApi
import vip.mystery0.xhu.timetable.base.BaseDataRepo
import vip.mystery0.xhu.timetable.base.buildPageSource
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
            pagingSourceFactory = {
                buildPageSource { index, size ->
                    checkForceLoadFromCloud(true)

                    user.withAutoLoginOnce {
                        customThingApi.customThingList(it, index, size)
                    }
                }
            }
        ).flow

    suspend fun createCustomThing(
        user: User,
        request: CustomThingRequest,
    ) {
        val response = user.withAutoLoginOnce {
            customThingApi.createCustomThing(it, request)
        }
        if (!response) {
            throw RuntimeException("创建自定义事项失败")
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
            throw RuntimeException("更新自定义事项失败")
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
            throw RuntimeException("删除自定义事项失败")
        }
    }
}