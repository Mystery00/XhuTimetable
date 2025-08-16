package vip.mystery0.xhu.timetable.viewmodel

import androidx.lifecycle.viewModelScope
import com.maxkeppeker.sheets.core.models.base.UseCaseState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import vip.mystery0.xhu.timetable.base.ComposeViewModel
import vip.mystery0.xhu.timetable.config.Customisable
import vip.mystery0.xhu.timetable.config.coroutine.safeLaunch
import vip.mystery0.xhu.timetable.config.networkErrorHandler
import vip.mystery0.xhu.timetable.config.store.EventBus
import vip.mystery0.xhu.timetable.config.store.GlobalConfigStore
import vip.mystery0.xhu.timetable.config.store.UserStore
import vip.mystery0.xhu.timetable.config.store.UserStore.withAutoLoginOnce
import vip.mystery0.xhu.timetable.config.store.getConfigStore
import vip.mystery0.xhu.timetable.config.store.setConfigStore
import vip.mystery0.xhu.timetable.model.CampusInfo
import vip.mystery0.xhu.timetable.model.event.EventType
import vip.mystery0.xhu.timetable.module.desc
import vip.mystery0.xhu.timetable.repository.UserRepo
import vip.mystery0.xhu.timetable.utils.MIN
import vip.mystery0.xhu.timetable.utils.now

class ClassSettingsViewModel : ComposeViewModel() {
    private val _selectYearAndTermList = MutableStateFlow<List<String>>(emptyList())
    val selectYearAndTermList: StateFlow<List<String>> = _selectYearAndTermList

    private val _currentYearData = MutableStateFlow(GlobalConfigStore.customNowYear)
    val currentYearData: StateFlow<Customisable<Int>> = _currentYearData
    private val _currentTermData = MutableStateFlow(GlobalConfigStore.customNowTerm)
    val currentTermData: StateFlow<Customisable<Int>> = _currentTermData
    private val _campusInfo = MutableStateFlow(CampusInfo.EMPTY)
    val campusInfo: StateFlow<CampusInfo> = _campusInfo
    private val _showTomorrowCourseTimeData = MutableStateFlow<LocalTime?>(null)
    val showTomorrowCourseTimeData: StateFlow<LocalTime?> = _showTomorrowCourseTimeData
    private val _currentTermStartTime = MutableStateFlow(GlobalConfigStore.customTermStartDate)
    val currentTermStartTime: StateFlow<Customisable<LocalDate>> = _currentTermStartTime
    private val _showCustomCourseData = MutableStateFlow(false)
    val showCustomCourseData: StateFlow<Boolean> = _showCustomCourseData
    private val _showCustomThingData = MutableStateFlow(false)
    val showCustomThingData: StateFlow<Boolean> = _showCustomThingData

    val showTomorrowCourseTimeState = MutableStateFlow(UseCaseState())
    val yearAndTermState = MutableStateFlow(UseCaseState())
    val termStartTimeState = MutableStateFlow(UseCaseState())
    val userCampusState = MutableStateFlow(UseCaseState())

    fun init() {
        viewModelScope.safeLaunch {
            loadCampusList()
            _showTomorrowCourseTimeData.value = getConfigStore { showTomorrowCourseTime }
            _showCustomCourseData.value = getConfigStore { showCustomCourseOnWeek }
            _showCustomThingData.value = getConfigStore { showCustomThing }

            _selectYearAndTermList.value = withContext(Dispatchers.Default) {
                val loggedUserList = UserStore.loggedUserList()
                val startYear =
                    loggedUserList.minByOrNull { it.info.xhuGrade }?.info?.xhuGrade ?: 2019
                val endYear = LocalDate.now().year
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

    private fun loadCampusList() {
        viewModelScope.safeLaunch(onException = networkErrorHandler { throwable ->
            logger.w("load campus list failed", throwable)
            toastMessage(throwable.desc())
        }) {
            val mainUser = UserStore.mainUser()
            _campusInfo.value = mainUser.withAutoLoginOnce {
                UserRepo.getCampusList(it)
            }
        }
    }

    fun updateUserCampus(campus: String) {
        viewModelScope.safeLaunch(onException = networkErrorHandler { throwable ->
            logger.w("load campus list failed", throwable)
            toastMessage(throwable.desc())
        }) {
            val mainUser = UserStore.mainUser()
            mainUser.withAutoLoginOnce {
                UserRepo.updateUserCampus(it, campus)
            }
            EventBus.post(EventType.CHANGE_CAMPUS)
            loadCampusList()
        }
    }

    fun updateShowTomorrowCourseTime(time: LocalTime?) {
        viewModelScope.safeLaunch {
            setConfigStore { showTomorrowCourseTime = time }
            _showTomorrowCourseTimeData.value = getConfigStore { showTomorrowCourseTime }
            EventBus.post(EventType.CHANGE_AUTO_SHOW_TOMORROW_COURSE)
        }
    }

    fun updateCurrentYearTerm(custom: Boolean, year: Int = -1, term: Int = -1) {
        viewModelScope.safeLaunch {
            setConfigStore {
                customNowYear =
                    if (custom) Customisable.custom(year) else Customisable.clearCustom(-1)
                customNowTerm =
                    if (custom) Customisable.custom(term) else Customisable.clearCustom(-1)
            }
            _currentYearData.value = getConfigStore { customNowYear }
            _currentTermData.value = getConfigStore { customNowTerm }
            EventBus.post(EventType.CHANGE_CURRENT_YEAR_AND_TERM)
        }
    }

    fun updateTermStartTime(custom: Boolean, date: LocalDate) {
        viewModelScope.safeLaunch {
            setConfigStore {
                customTermStartDate =
                    if (custom) Customisable.custom(date) else Customisable.clearCustom(LocalDate.MIN)
            }
            _currentTermStartTime.value = getConfigStore { customTermStartDate }
            EventBus.post(EventType.CHANGE_TERM_START_TIME)
        }
    }

    fun updateShowCustomCourse(showCustomCourseData: Boolean) {
        viewModelScope.safeLaunch {
            setConfigStore { showCustomCourseOnWeek = showCustomCourseData }
            _showCustomCourseData.value = getConfigStore { showCustomCourseOnWeek }
            EventBus.post(EventType.CHANGE_SHOW_CUSTOM_COURSE)
        }
    }

    fun updateShowCustomThing(showCustomThingData: Boolean) {
        viewModelScope.safeLaunch {
            setConfigStore { showCustomThing = showCustomThingData }
            _showCustomThingData.value = getConfigStore { showCustomThing }
            EventBus.post(EventType.CHANGE_SHOW_CUSTOM_THING)
        }
    }
}