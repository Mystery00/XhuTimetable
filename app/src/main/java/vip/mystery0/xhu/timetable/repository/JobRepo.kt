package vip.mystery0.xhu.timetable.repository

import org.koin.core.component.inject
import vip.mystery0.xhu.timetable.api.JobApi
import vip.mystery0.xhu.timetable.base.BaseDataRepo
import vip.mystery0.xhu.timetable.config.store.UserStore.withAutoLoginOnce

object JobRepo : BaseDataRepo {
    private val jobApi: JobApi by inject()

    suspend fun testPush(registrationId: String) {
        mainUser().withAutoLoginOnce {
            jobApi.pushTest(it, registrationId)
        }
    }
}