package vip.mystery0.xhu.timetable.repository

import androidx.annotation.AnyThread
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.PagingSource
import androidx.paging.PagingState
import kotlinx.coroutines.flow.Flow
import org.koin.core.component.inject
import vip.mystery0.xhu.timetable.api.CourseApi
import vip.mystery0.xhu.timetable.base.BaseDataRepo
import vip.mystery0.xhu.timetable.config.store.User
import vip.mystery0.xhu.timetable.config.store.UserStore.withAutoLoginOnce
import vip.mystery0.xhu.timetable.model.request.AllCourseRequest
import vip.mystery0.xhu.timetable.model.response.AllCourseResponse

object CourseRepo : BaseDataRepo {
    private val courseApi: CourseApi by inject()

    private val globalPagingConfig: PagingConfig
        @AnyThread get() = PagingConfig(
            pageSize = 10,
            initialLoadSize = 10,
        )

    fun getAllCourseListStream(
        user: User,
        year: Int,
        term: Int,
        request: AllCourseRequest,
    ): Flow<PagingData<AllCourseResponse>> =
        Pager(
            config = globalPagingConfig,
            pagingSourceFactory = {
                AllCoursePageSource(courseApi, user, year, term, request)
            }
        ).flow

    internal class AllCoursePageSource(
        private val courseApi: CourseApi,
        private val user: User,
        private val year: Int,
        private val term: Int,
        private val request: AllCourseRequest,
    ) : PagingSource<Int, AllCourseResponse>() {
        override fun getRefreshKey(state: PagingState<Int, AllCourseResponse>): Int? =
            state.anchorPosition?.let {
                val anchorPage = state.closestPageToPosition(it)
                anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
            }

        override suspend fun load(params: LoadParams<Int>): LoadResult<Int, AllCourseResponse> {
            checkForceLoadFromCloud(true)

            val index = params.key ?: 0
            val response = user.withAutoLoginOnce {
                courseApi.allCourseList(it, year, term, index, params.loadSize, request)
            }
            return LoadResult.Page(
                data = response.items,
                prevKey = null,
                nextKey = response.hasNext.let { if (it) index + 1 else null },
            )
        }

    }
}
