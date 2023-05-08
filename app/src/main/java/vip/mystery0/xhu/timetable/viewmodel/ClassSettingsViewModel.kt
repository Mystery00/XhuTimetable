package vip.mystery0.xhu.timetable.viewmodel

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.koin.core.component.inject
import vip.mystery0.xhu.timetable.base.ComposeViewModel
import vip.mystery0.xhu.timetable.config.Customisable
import vip.mystery0.xhu.timetable.config.chinaZone
import vip.mystery0.xhu.timetable.config.getConfig
import vip.mystery0.xhu.timetable.config.runOnCpu
import vip.mystery0.xhu.timetable.config.setConfig
import vip.mystery0.xhu.timetable.config.store.GlobalConfigStore
import vip.mystery0.xhu.timetable.config.store.UserStore
import vip.mystery0.xhu.timetable.config.store.getConfigStore
import vip.mystery0.xhu.timetable.config.store.setConfigStore
import vip.mystery0.xhu.timetable.model.event.EventType
import vip.mystery0.xhu.timetable.model.event.UIEvent
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.Month
import kotlin.math.max

class ClassSettingsViewModel : ComposeViewModel() {
    private val eventBus: EventBus by inject()

    private val _errorMessage = MutableStateFlow("")
    val errorMessage: StateFlow<String> = _errorMessage

    private val _selectYearAndTermList = MutableStateFlow<List<String>>(emptyList())
    val selectYearAndTermList: StateFlow<List<String>> = _selectYearAndTermList

    private val _currentYearData = MutableStateFlow(GlobalConfigStore.customNowYear)
    val currentYearData: StateFlow<Customisable<Int>> = _currentYearData
    private val _currentTermData = MutableStateFlow(GlobalConfigStore.customNowTerm)
    val currentTermData: StateFlow<Customisable<Int>> = _currentTermData
    private val _showTomorrowCourseTimeData = MutableStateFlow<LocalTime?>(null)
    val showTomorrowCourseTimeData: StateFlow<LocalTime?> = _showTomorrowCourseTimeData
    private val _currentTermStartTime = MutableStateFlow(GlobalConfigStore.customTermStartDate)
    val currentTermStartTime: StateFlow<Customisable<LocalDate>> = _currentTermStartTime
    private val _showCustomCourseData = MutableStateFlow(false)
    val showCustomCourseData: StateFlow<Boolean> = _showCustomCourseData
    private val _showCustomThingData = MutableStateFlow(false)
    val showCustomThingData: StateFlow<Boolean> = _showCustomThingData

    init {
        viewModelScope.launch {
            _showTomorrowCourseTimeData.value = getConfig { showTomorrowCourseTime }
            val customTermStartTime = getConfig { customTermStartTime }
            _showCustomCourseData.value = getConfig { showCustomCourseOnWeek }
            _showCustomThingData.value = getConfig { showCustomThing }

            val loggedUserList = UserStore.loggedUserList()
            _selectYearAndTermList.value = runOnCpu {
                var startGrade = loggedUserList.minByOrNull { it.info.xhuGrade }?.info?.xhuGrade
                var endGrade = loggedUserList.maxByOrNull { it.info.xhuGrade }?.info?.xhuGrade
                if (startGrade == null) {
                    startGrade = 2019
                }
                val startYear = startGrade.toInt()
                val time = LocalDateTime.ofInstant(getConfig { termStartTime }, chinaZone)
                val nowEndYear = if (time.month < Month.JUNE) time.year - 1 else time.year
                if (endGrade == null) {
                    endGrade = nowEndYear
                }
                val endYear = endGrade.toInt() + 3
                val tempArrayList = ArrayList<String>()
                for (it in startYear..max(nowEndYear, endYear)) {
                    tempArrayList.add("${it}-${it + 1}学年 第1学期")
                    tempArrayList.add("${it}-${it + 1}学年 第2学期")
                }
                tempArrayList.sortDescending()
                tempArrayList.add(0, "自动获取")
                tempArrayList
            }
        }
    }

    fun updateShowTomorrowCourseTime(time: LocalTime?) {
        viewModelScope.launch {
            setConfig {
                showTomorrowCourseTime = time
            }
            _showTomorrowCourseTimeData.value = getConfig { showTomorrowCourseTime }
            eventBus.post(UIEvent(EventType.CHANGE_AUTO_SHOW_TOMORROW_COURSE))
        }
    }

    fun updateCurrentYearTerm(custom: Boolean, year: Int = -1, term: Int = -1) {
        viewModelScope.launch {
            setConfigStore {
                customNowYear =
                    if (custom) Customisable.custom(year) else Customisable.clearCustom(-1)
                customNowTerm =
                    if (custom) Customisable.custom(term) else Customisable.clearCustom(-1)
            }
            _currentYearData.value = getConfigStore { customNowYear }
            _currentTermData.value = getConfigStore { customNowTerm }
            eventBus.post(UIEvent(EventType.CHANGE_CURRENT_YEAR_AND_TERM))
        }
    }

    fun updateTermStartTime(custom: Boolean, date: LocalDate) {
        viewModelScope.launch {
            setConfigStore {
                customTermStartDate =
                    if (custom) Customisable.custom(date) else Customisable.clearCustom(LocalDate.MIN)
            }
            _currentTermStartTime.value = getConfigStore { customTermStartDate }
            eventBus.post(UIEvent(EventType.CHANGE_TERM_START_TIME))
        }
    }

    fun updateShowCustomCourse(showCustomCourseData: Boolean) {
        viewModelScope.launch {
            setConfig {
                showCustomCourseOnWeek = showCustomCourseData
            }
            _showCustomCourseData.value = getConfig { showCustomCourseOnWeek }
            eventBus.post(UIEvent(EventType.CHANGE_SHOW_CUSTOM_COURSE))
        }
    }

    fun updateShowCustomThing(showCustomThingData: Boolean) {
        viewModelScope.launch {
            setConfig {
                showCustomThing = showCustomThingData
            }
            _showCustomThingData.value = getConfig { showCustomThing }
            eventBus.post(UIEvent(EventType.CHANGE_SHOW_CUSTOM_THING))
        }
    }
}