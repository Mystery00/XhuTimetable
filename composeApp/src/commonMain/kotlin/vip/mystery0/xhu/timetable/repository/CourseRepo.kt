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
import vip.mystery0.xhu.timetable.config.store.UserStore.withAutoLoginOnce
import vip.mystery0.xhu.timetable.model.request.SchoolTimetableRequest
import vip.mystery0.xhu.timetable.model.response.SchoolTimetableResponse

object CourseRepo : BaseDataRepo {
    private val courseApi: CourseApi by inject()

    private val globalPagingConfig: PagingConfig
        @AnyThread get() = PagingConfig(
            pageSize = 10,
            initialLoadSize = 10,
        )

    fun getAllCourseListStream(
        request: SchoolTimetableRequest,
    ): Flow<PagingData<SchoolTimetableResponse>> =
        Pager(
            config = globalPagingConfig,
            pagingSourceFactory = {
                buildPageSource { index, size ->
                    checkForceLoadFromCloud(true)

                    mainUser().withAutoLoginOnce {
                        courseApi.allCourseList(it, index, size, request)
                    }
                }
            }
        ).flow

    suspend fun loadCampusList(): Map<String, String> =
        mainUser().withAutoLoginOnce {
            courseApi.campusList(it)
        }

    suspend fun loadCollegeList(): Map<String, String> =
        mainUser().withAutoLoginOnce {
            courseApi.collegeList(it)
        }

    suspend fun loadMajorList(collegeId: String): Map<String, String> =
        mainUser().withAutoLoginOnce {
            courseApi.majorList(it, collegeId)
        }
}
