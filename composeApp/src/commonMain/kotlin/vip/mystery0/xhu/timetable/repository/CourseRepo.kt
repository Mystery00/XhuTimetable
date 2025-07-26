package vip.mystery0.xhu.timetable.repository

import androidx.annotation.AnyThread
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import org.koin.core.component.inject
import vip.mystery0.xhu.timetable.api.CourseApi
import vip.mystery0.xhu.timetable.base.BaseDataRepo
import vip.mystery0.xhu.timetable.base.buildPageSource
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
                buildPageSource { index, size ->
                    checkForceLoadFromCloud(true)

                    user.withAutoLoginOnce {
                        courseApi.allCourseList(it, year, term, index, size, request)
                    }
                }
            }
        ).flow
}
