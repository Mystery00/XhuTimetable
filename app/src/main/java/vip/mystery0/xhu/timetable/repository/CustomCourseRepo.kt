package vip.mystery0.xhu.timetable.repository

import androidx.annotation.AnyThread
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.PagingSource
import androidx.paging.PagingState
import kotlinx.coroutines.flow.Flow
import org.koin.core.component.inject
import vip.mystery0.xhu.timetable.api.CustomCourseApi
import vip.mystery0.xhu.timetable.base.BaseDataRepo
import vip.mystery0.xhu.timetable.config.store.User
import vip.mystery0.xhu.timetable.config.store.UserStore.withAutoLoginOnce
import vip.mystery0.xhu.timetable.model.response.CustomCourseResponse

object CustomCourseRepo : BaseDataRepo {
    private val customCourseApi: CustomCourseApi by inject()

    private val globalPagingConfig: PagingConfig
        @AnyThread get() = PagingConfig(
            pageSize = 10,
        )

    fun getCustomCourseListStream(
        user: User,
        year: Int,
        term: Int
    ): Flow<PagingData<CustomCourseResponse>> =
        Pager(
            config = globalPagingConfig,
            pagingSourceFactory = { CustomCoursePageSource(customCourseApi, user, year, term) }
        ).flow
}

class CustomCoursePageSource(
    private val customCourseApi: CustomCourseApi,
    private val user: User,
    private val year: Int,
    private val term: Int,
) : PagingSource<Int, CustomCourseResponse>() {
    override fun getRefreshKey(state: PagingState<Int, CustomCourseResponse>): Int? =
        state.anchorPosition?.let {
            val anchorPage = state.closestPageToPosition(it)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, CustomCourseResponse> {
        CustomCourseRepo.checkForceLoadFromCloud(true)

        val index = params.key ?: 0
        val response = user.withAutoLoginOnce {
            customCourseApi.customCourseList(it, year, term, index, 10)
        }
        return LoadResult.Page(
            data = response.items,
            prevKey = null,// Only paging forward.
            nextKey = response.hasNext.let { if (it) index + 1 else null },
        )
    }
}