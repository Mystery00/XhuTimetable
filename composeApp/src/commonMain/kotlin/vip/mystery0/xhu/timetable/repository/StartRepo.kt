package vip.mystery0.xhu.timetable.repository

import co.touchlab.kermit.Logger
import io.ktor.client.call.body
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import org.koin.core.component.inject
import vip.mystery0.xhu.timetable.api.CommonApi
import vip.mystery0.xhu.timetable.api.MenuApi
import vip.mystery0.xhu.timetable.base.BaseDataRepo
import vip.mystery0.xhu.timetable.config.Customisable
import vip.mystery0.xhu.timetable.config.coroutine.safeLaunch
import vip.mystery0.xhu.timetable.config.coroutine.safeWithContext
import vip.mystery0.xhu.timetable.config.store.MenuStore
import vip.mystery0.xhu.timetable.config.store.getCacheStore
import vip.mystery0.xhu.timetable.config.store.getConfigStore
import vip.mystery0.xhu.timetable.config.store.setCacheStore
import vip.mystery0.xhu.timetable.config.store.setConfigStore
import vip.mystery0.xhu.timetable.model.request.ClientInitRequest
import vip.mystery0.xhu.timetable.model.response.ClientVersion
import vip.mystery0.xhu.timetable.model.response.TeamMemberResponse
import vip.mystery0.xhu.timetable.model.response.VersionUrl
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.time.DurationUnit
import kotlin.time.toDuration

object StartRepo : BaseDataRepo {
    private val logger = Logger.withTag("StartRepo")
    private val commonApi: CommonApi by inject()
    private val menuApi: MenuApi by inject()
    private val repoScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    val version = MutableSharedFlow<ClientVersion>(
        replay = 1,
        extraBufferCapacity = 0,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    suspend fun init() {
        if (!isOnline) {
            localInit()
            return
        }
        val clientInitResponse = withTimeoutOrNull(3.toDuration(DurationUnit.SECONDS)) {
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
        val newVersion = clientInitResponse.latestVersion?.let { version ->
            val ignoreList = getCacheStore { ignoreVersionList }
            val versionString = "${version.versionName}-${version.versionCode}"
            if (!ignoreList.contains(versionString)) {
                version
            } else {
                null
            }
        }
        version.emit(newVersion ?: ClientVersion.EMPTY)

        //处理菜单
        initMenu()

        //处理节假日数据
        val nowHoliday = clientInitResponse.holiday
        val tomorrowHoliday = clientInitResponse.tomorrowHoliday
        setCacheStore { holiday = (nowHoliday to tomorrowHoliday) }
    }

    suspend fun initMenu() {
        val menuList = menuApi.list()
        MenuStore.updateList(menuList.map { it.toMenu() })
    }

    private fun localInit() {
        logger.i("Initializing with local data due to network issue or timeout.")
        repoScope.safeLaunch {
            version.emit(ClientVersion.EMPTY)
        }
    }

    suspend fun checkVersion(forceBeta: Boolean) {
        if (!isOnline) {
            return
        }
        val versionChannel = getConfigStore { versionChannel }
        val alwaysShowNewVersion = getCacheStore { alwaysShowNewVersion }
        val versionResp = commonApi.checkVersion(
            if (forceBeta) true else versionChannel.isBeta(),
            alwaysShowNewVersion,
        )
        logger.d("checkVersion: ${versionResp.status}")
        if (versionResp.status == HttpStatusCode.NoContent) {
            return this.version.emit(ClientVersion.EMPTY)
        }
        val version = versionResp.body<ClientVersion>()
        logger.d("checkVersion: $version")
        val ignoreList = getCacheStore { ignoreVersionList }
        val versionString = "${version.versionName}-${version.versionCode}"
        if (!ignoreList.contains(versionString)) {
            this.version.emit(version)
        } else {
            this.version.emit(ClientVersion.EMPTY)
        }
    }

    suspend fun getVersionUrl(versionId: Long): VersionUrl =
        withContext(Dispatchers.IO) {
            commonApi.getVersionUrl(versionId)
        }

    suspend fun loadTeamMemberList(): List<TeamMemberResponse> {
        if (!isOnline) {
            return getCacheStore { teamMemberList }
        }
        return safeWithContext(EmptyCoroutineContext, onException = {
            Logger.w("loadTeamMemberList failed: ${it.message}")
            true
        }, resultWhenException = { getCacheStore { teamMemberList } }) {
            val teamMemberList = commonApi.getTeamMemberList()
            setCacheStore {
                this.teamMemberList = teamMemberList
            }
            teamMemberList
        }
    }
}