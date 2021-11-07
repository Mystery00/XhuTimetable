package vip.mystery0.xhu.timetable.viewmodel

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.koin.core.component.inject
import vip.mystery0.xhu.timetable.base.ComposeViewModel
import vip.mystery0.xhu.timetable.config.*
import vip.mystery0.xhu.timetable.model.event.EventType
import vip.mystery0.xhu.timetable.model.event.UIEvent
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Month

class ClassSettingsViewModel : ComposeViewModel() {
    private val eventBus: EventBus by inject()

    private val _errorMessage = MutableStateFlow("")
    val errorMessage: StateFlow<String> = _errorMessage

    private val _selectYearAndTermList = MutableStateFlow<List<String>>(emptyList())
    val selectYearAndTermList: StateFlow<List<String>> = _selectYearAndTermList

    private val _currentYearData = MutableStateFlow(GlobalConfig.currentYearData)
    val currentYearData: StateFlow<Pair<String, Boolean>> = _currentYearData
    private val _currentTermData = MutableStateFlow(GlobalConfig.currentTermData)
    val currentTermData: StateFlow<Pair<Int, Boolean>> = _currentTermData
    private val _currentTermStartTime =
        MutableStateFlow(Pair<LocalDate, Boolean>(LocalDate.now(), false))
    val currentTermStartTime: StateFlow<Pair<LocalDate, Boolean>> = _currentTermStartTime

    init {
        viewModelScope.launch {
            val customTermStartTime = getConfig { customTermStartTime }
            _currentTermStartTime.value =
                Pair(
                    customTermStartTime.first.atZone(chinaZone).toLocalDate(),
                    customTermStartTime.second
                )

            val loggedUserList = SessionManager.loggedUserList()
            _selectYearAndTermList.value = runOnCpu {
                val startYear = loggedUserList.minByOrNull { it.info.grade }!!.info.grade.toInt()
                val time = LocalDateTime.ofInstant(getConfig { termStartTime }, chinaZone)
                val endYear = if (time.month < Month.JUNE) time.year - 1 else time.year
                val tempArrayList = ArrayList<String>()
                for (it in startYear..endYear) {
                    tempArrayList.add("${it}-${it + 1}学年 第1学期")
                    tempArrayList.add("${it}-${it + 1}学年 第2学期")
                }
                tempArrayList.sortDescending()
                tempArrayList.add(0, "自动获取")
                tempArrayList
            }
        }
    }

    fun updateCurrentYearTerm(year: String = "", term: Int = -1) {
        viewModelScope.launch {
            setConfig {
                currentYearData = year to (year != "")
                currentTermData = term to (term != -1)
            }
            _currentYearData.value = getConfig { currentYearData }
            _currentTermData.value = getConfig { currentTermData }
            eventBus.post(UIEvent(EventType.CHANGE_CURRENT_YEAR_AND_TERM))
        }
    }

    fun updateTermStartTime(date: LocalDate) {
        viewModelScope.launch {
            val customDate =
                if (date == LocalDate.MIN)
                    Instant.ofEpochMilli(0L)
                else
                    date.atStartOfDay(chinaZone).toInstant()
            setConfig {
                customTermStartTime = customDate to true
            }
            val customTermStartTime = getConfig { customTermStartTime }
            _currentTermStartTime.value =
                Pair(
                    customTermStartTime.first.atZone(chinaZone).toLocalDate(),
                    customTermStartTime.second
                )
            eventBus.post(UIEvent(EventType.CHANGE_TERM_START_TIME))
        }
    }
}