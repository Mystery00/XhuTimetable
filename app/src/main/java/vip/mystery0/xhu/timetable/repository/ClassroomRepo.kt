package vip.mystery0.xhu.timetable.repository

import androidx.annotation.AnyThread
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import org.koin.core.component.inject
import vip.mystery0.xhu.timetable.api.ClassroomApi
import vip.mystery0.xhu.timetable.base.BaseDataRepo
import vip.mystery0.xhu.timetable.base.buildPageSource
import vip.mystery0.xhu.timetable.config.store.UserStore.withAutoLoginOnce
import vip.mystery0.xhu.timetable.model.request.ClassroomRequest
import vip.mystery0.xhu.timetable.model.response.ClassroomResponse

object ClassroomRepo : BaseDataRepo {
    private val classroomApi: ClassroomApi by inject()

    private val globalPagingConfig: PagingConfig
        @AnyThread get() = PagingConfig(
            pageSize = 10,
            initialLoadSize = 10,
        )

    fun getClassroomListStream(request: ClassroomRequest): Flow<PagingData<ClassroomResponse>> =
        Pager(
            config = globalPagingConfig,
            pagingSourceFactory = {
                buildPageSource { index, size ->
                    checkForceLoadFromCloud(true)

                    mainUser().withAutoLoginOnce {
                        classroomApi.getFreeList(it, request, index, size)
                    }
                }
            }
        ).flow
}