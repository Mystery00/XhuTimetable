package vip.mystery0.xhu.timetable.repository

import androidx.annotation.AnyThread
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.PagingSource
import androidx.paging.PagingState
import kotlinx.coroutines.flow.Flow
import org.koin.core.component.inject
import vip.mystery0.xhu.timetable.api.ClassroomApi
import vip.mystery0.xhu.timetable.base.BaseDataRepo
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
            pagingSourceFactory = { ClassroomPageSource(classroomApi, request) }
        ).flow

    internal class ClassroomPageSource(
        private val classroomApi: ClassroomApi,
        private val request: ClassroomRequest,
    ) : PagingSource<Int, ClassroomResponse>() {
        override fun getRefreshKey(state: PagingState<Int, ClassroomResponse>): Int? =
            state.anchorPosition?.let {
                val anchorPage = state.closestPageToPosition(it)
                anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
            }

        override suspend fun load(params: LoadParams<Int>): LoadResult<Int, ClassroomResponse> {
            checkForceLoadFromCloud(true)

            val index = params.key ?: 0
            val response = mainUser().withAutoLoginOnce {
                classroomApi.getFreeList(it, request, index, params.loadSize)
            }
            return LoadResult.Page(
                data = response.items,
                prevKey = null,
                nextKey = response.hasNext.let { if (it) index + 1 else null },
            )
        }
    }
}