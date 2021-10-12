package vip.mystery0.xhu.timetable.viewmodel

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.greenrobot.eventbus.EventBus
import org.koin.core.component.inject
import vip.mystery0.xhu.timetable.base.ComposeViewModel
import vip.mystery0.xhu.timetable.config.SessionManager
import vip.mystery0.xhu.timetable.config.chinaZone
import vip.mystery0.xhu.timetable.config.getConfig
import vip.mystery0.xhu.timetable.config.runOnCpu
import java.time.LocalDateTime
import java.time.Month

class ClassSettingsViewModel : ComposeViewModel() {
    private val eventBus: EventBus by inject()

    private val _errorMessage = MutableStateFlow("")
    val errorMessage: StateFlow<String> = _errorMessage

    suspend fun buildSelect(): IntRange {
        val loggedUserList = SessionManager.loggedUserList()
        return runOnCpu {
            val startYear = loggedUserList.minByOrNull { it.info.grade }!!.info.grade.toInt()
            val time = LocalDateTime.ofInstant(getConfig { termStartTime }, chinaZone)
            val endYear = if (time.month < Month.JUNE) time.year - 1 else time.year
            startYear..endYear
        }
    }
}