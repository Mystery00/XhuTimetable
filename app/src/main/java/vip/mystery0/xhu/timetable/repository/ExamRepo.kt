package vip.mystery0.xhu.timetable.repository

import org.koin.core.component.inject
import vip.mystery0.xhu.timetable.api.ExamApi
import vip.mystery0.xhu.timetable.base.BaseDataRepo
import vip.mystery0.xhu.timetable.config.store.User
import vip.mystery0.xhu.timetable.config.store.UserStore.withAutoLoginOnce
import vip.mystery0.xhu.timetable.config.store.getConfigStore
import vip.mystery0.xhu.timetable.model.response.ExamResponse

object ExamRepo : BaseDataRepo {
    private val examApi: ExamApi by inject()

    suspend fun fetchExamList(user: User): List<ExamResponse> {
        checkForceLoadFromCloud(true)

        val nowYear = getConfigStore { nowYear }
        val nowTerm = getConfigStore { nowTerm }
        val response = user.withAutoLoginOnce {
            examApi.examList(it, nowYear, nowTerm)
        }
        return response
    }
}