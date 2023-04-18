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
import vip.mystery0.xhu.timetable.config.ServerError
import vip.mystery0.xhu.timetable.config.store.User
import vip.mystery0.xhu.timetable.config.store.UserStore.withAutoLoginOnce
import vip.mystery0.xhu.timetable.model.request.CustomCourseRequest
import vip.mystery0.xhu.timetable.model.response.CustomCourseResponse

object CustomCourseRepo : BaseDataRepo {
    private val customCourseApi: CustomCourseApi by inject()

    private val globalPagingConfig: PagingConfig
        @AnyThread get() = PagingConfig(
            pageSize = 10,
            initialLoadSize = 10,
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

    suspend fun createCustomCourse(
        user: User,
        request: CustomCourseRequest,
    ) {
        val response = user.withAutoLoginOnce {
            customCourseApi.createCustomCourse(it, request)
        }
        if (!response) {
            throw ServerError("创建自定义课程失败")
        }
    }

    suspend fun updateCustomCourse(
        user: User,
        courseId: Long,
        request: CustomCourseRequest,
    ) {
        val response = user.withAutoLoginOnce {
            customCourseApi.updateCustomCourse(it, courseId, request)
        }
        if (!response) {
            throw ServerError("更新自定义课程失败")
        }
    }

    suspend fun deleteCustomCourse(
        user: User,
        courseId: Long,
    ) {
        val response = user.withAutoLoginOnce {
            customCourseApi.deleteCustomCourse(it, courseId)
        }
        if (!response) {
            throw ServerError("删除自定义课程失败")
        }
    }

    internal class CustomCoursePageSource(
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
            checkForceLoadFromCloud(true)

            val index = params.key ?: 0
            val response = user.withAutoLoginOnce {
                customCourseApi.customCourseList(it, year, term, index, params.loadSize)
            }
            return LoadResult.Page(
                data = response.items,
                prevKey = null,
                nextKey = response.hasNext.let { if (it) index + 1 else null },
            )
        }
    }
}