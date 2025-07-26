package vip.mystery0.xhu.timetable.repository

import androidx.annotation.AnyThread
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import org.koin.core.component.inject
import vip.mystery0.xhu.timetable.api.CustomCourseApi
import vip.mystery0.xhu.timetable.base.BaseDataRepo
import vip.mystery0.xhu.timetable.base.buildPageSource
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
            pagingSourceFactory = {
                buildPageSource { index, size ->
                    checkForceLoadFromCloud(true)

                    user.withAutoLoginOnce {
                        customCourseApi.customCourseList(it, year, term, index, size)
                    }
                }
            }
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
}