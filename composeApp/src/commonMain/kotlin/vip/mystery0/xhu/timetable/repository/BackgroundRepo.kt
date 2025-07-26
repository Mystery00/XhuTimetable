package vip.mystery0.xhu.timetable.repository

import org.koin.core.component.inject
import vip.mystery0.xhu.timetable.api.BackgroundApi
import vip.mystery0.xhu.timetable.base.BaseDataRepo
import vip.mystery0.xhu.timetable.config.store.UserStore.withAutoLoginOnce
import vip.mystery0.xhu.timetable.model.response.BackgroundResponse

object BackgroundRepo : BaseDataRepo {
    private val backgroundApi: BackgroundApi by inject()

    suspend fun getList(): List<BackgroundResponse> {
        checkForceLoadFromCloud(true)

        return mainUser().withAutoLoginOnce {
            backgroundApi.selectList(it)
        }
    }
}