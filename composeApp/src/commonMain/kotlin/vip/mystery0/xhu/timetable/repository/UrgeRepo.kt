package vip.mystery0.xhu.timetable.repository

import androidx.annotation.AnyThread
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import org.koin.core.component.inject
import vip.mystery0.xhu.timetable.api.UrgeApi
import vip.mystery0.xhu.timetable.base.BaseDataRepo
import vip.mystery0.xhu.timetable.base.buildPageSource
import vip.mystery0.xhu.timetable.config.store.UserStore.withAutoLoginOnce
import vip.mystery0.xhu.timetable.model.response.UrgeItem
import vip.mystery0.xhu.timetable.model.transfer.PageResult

object UrgeRepo : BaseDataRepo {
    private val urgeApi: UrgeApi by inject()

    val remainCountFlow = MutableStateFlow(0)

    private val globalPagingConfig: PagingConfig
        @AnyThread get() = PagingConfig(
            pageSize = 10,
            initialLoadSize = 10,
        )

    fun getUrgeListStream(): Flow<PagingData<UrgeItem>> = Pager(
        config = globalPagingConfig,
        pagingSourceFactory = {
            buildPageSource { index, size -> getUrgePage(index, size) }
        }
    ).flow

    suspend fun getUrgePage(index: Int, size: Int):
        PageResult<UrgeItem> {
        checkForceLoadFromCloud(true)
        val urge = mainUser().withAutoLoginOnce { urgeApi.getUrgeList(it, index = index, size = size) }
        remainCountFlow.emit(urge.remainCount)
        return urge.page
    }

    suspend fun doUrge(urgeId: Long) {
        checkForceLoadFromCloud(true)

        mainUser().withAutoLoginOnce {
            urgeApi.urge(it, urgeId)
        }
    }
}