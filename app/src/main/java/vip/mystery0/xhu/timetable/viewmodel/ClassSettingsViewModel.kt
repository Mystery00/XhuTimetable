package vip.mystery0.xhu.timetable.viewmodel

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.greenrobot.eventbus.EventBus
import org.koin.core.component.inject
import vip.mystery0.xhu.timetable.base.ComposeViewModel
import vip.mystery0.xhu.timetable.config.Customisable
import vip.mystery0.xhu.timetable.config.store.GlobalConfigStore
import vip.mystery0.xhu.timetable.config.store.UserStore
import vip.mystery0.xhu.timetable.config.store.getConfigStore
import vip.mystery0.xhu.timetable.config.store.setConfigStore
import vip.mystery0.xhu.timetable.model.event.EventType
import vip.mystery0.xhu.timetable.model.event.UIEvent
import java.time.LocalDate
import java.time.LocalTime
import java.time.Month

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
            _showTomorrowCourseTimeData.value = getConfigStore { showTomorrowCourseTime }
            _showCustomCourseData.value = getConfigStore { showCustomCourseOnWeek }
            _showCustomThingData.value = getConfigStore { showCustomThing }

            _selectYearAndTermList.value = withContext(Dispatchers.Default) {
                val loggedUserList = UserStore.loggedUserList()
                val startYear =
                    loggedUserList.minByOrNull { it.info.xhuGrade }?.info?.xhuGrade ?: 2019
                val termStartDate = getConfigStore { termStartDate }
                val endYear = if (termStartDate.month < Month.JUNE) termStartDate.year - 1 else termStartDate.year
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

    fun updateShowTomorrowCourseTime(time: LocalTime?) {
        viewModelScope.launch {
            setConfigStore { showTomorrowCourseTime = time }
            _showTomorrowCourseTimeData.value = getConfigStore { showTomorrowCourseTime }
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
            setConfigStore { showCustomCourseOnWeek = showCustomCourseData }
            _showCustomCourseData.value = getConfigStore { showCustomCourseOnWeek }
            eventBus.post(UIEvent(EventType.CHANGE_SHOW_CUSTOM_COURSE))
        }
    }

    fun updateShowCustomThing(showCustomThingData: Boolean) {
        viewModelScope.launch {
            setConfigStore { showCustomThing = showCustomThingData }
            _showCustomThingData.value = getConfigStore { showCustomThing }
            eventBus.post(UIEvent(EventType.CHANGE_SHOW_CUSTOM_THING))
        }
    }
}