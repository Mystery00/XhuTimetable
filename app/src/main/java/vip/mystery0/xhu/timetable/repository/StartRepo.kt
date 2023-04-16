package vip.mystery0.xhu.timetable.repository

import kotlinx.coroutines.withTimeoutOrNull
import org.koin.core.component.inject
import vip.mystery0.xhu.timetable.api.CommonApi
import vip.mystery0.xhu.timetable.api.MenuApi
import vip.mystery0.xhu.timetable.base.BaseDataRepo
import vip.mystery0.xhu.timetable.config.Customisable
import vip.mystery0.xhu.timetable.config.store.MenuStore
import vip.mystery0.xhu.timetable.config.store.setConfigStore
import vip.mystery0.xhu.timetable.model.request.ClientInitRequest
import java.time.Duration

object StartRepo : BaseDataRepo {
    private val commonApi: CommonApi by inject()
    private val menuApi: MenuApi by inject()

    suspend fun init() {
        if (!isOnline) {
            localInit()
            return
        }
        val clientInitResponse = withTimeoutOrNull(Duration.ofSeconds(3).toMillis()) {
            commonApi.clientInit(ClientInitRequest())
        } ?: return localInit()
        val xhuStartTime = clientInitResponse.xhuStartTime
        setConfigStore {
            customTermStartDate = Customisable.serverDetect(xhuStartTime.startDate)
            splashList = clientInitResponse.splash
            customNowYear = Customisable.serverDetect(xhuStartTime.nowYear)
            customNowTerm = Customisable.serverDetect(xhuStartTime.nowTerm)
        }
        //TODO 新版本处理
        //处理菜单
        val menuList = menuApi.list()
        MenuStore.updateList(menuList.map { it.toMenu() })
    }

    private fun localInit() {
        //do nothing
    }
}