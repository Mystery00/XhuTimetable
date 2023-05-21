package vip.mystery0.xhu.timetable.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import org.koin.core.component.inject
import vip.mystery0.xhu.timetable.api.CommonApi
import vip.mystery0.xhu.timetable.api.MenuApi
import vip.mystery0.xhu.timetable.base.BaseDataRepo
import vip.mystery0.xhu.timetable.config.Customisable
import vip.mystery0.xhu.timetable.config.DataHolder
import vip.mystery0.xhu.timetable.config.store.MenuStore
import vip.mystery0.xhu.timetable.config.store.UserStore
import vip.mystery0.xhu.timetable.config.store.getCacheStore
import vip.mystery0.xhu.timetable.config.store.setCacheStore
import vip.mystery0.xhu.timetable.config.store.setConfigStore
import vip.mystery0.xhu.timetable.model.request.ClientInitRequest
import vip.mystery0.xhu.timetable.model.response.VersionUrl
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
            customNowYear = Customisable.serverDetect(xhuStartTime.nowYear)
            customNowTerm = Customisable.serverDetect(xhuStartTime.nowTerm)
        }
        setCacheStore {
            splashList = clientInitResponse.splash
        }
        //新版本处理
        clientInitResponse.latestVersion?.let { version ->
            val ignoreList = getCacheStore { ignoreVersionList }
            val versionString = "${version.versionName}-${version.versionCode}"
            if (!ignoreList.contains(versionString)) {
                DataHolder.version = version
            }
        }
        //处理菜单
        val menuList = menuApi.list()
        MenuStore.updateList(menuList.map { it.toMenu() })
        //处理用户信息
        DataHolder.mainUserName = UserStore.getMainUser()?.info?.name ?: "未登录"
    }

    private fun localInit() {
        //do nothing
    }

    suspend fun getVersionUrl(versionId: Long): VersionUrl =
        withContext(Dispatchers.IO) {
            commonApi.getVersionUrl(versionId)
        }
}