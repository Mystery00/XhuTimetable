package vip.mystery0.xhu.timetable.viewmodel

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import vip.mystery0.xhu.timetable.base.ComposeViewModel
import vip.mystery0.xhu.timetable.config.coroutine.safeLaunch
import vip.mystery0.xhu.timetable.config.store.EventBus
import vip.mystery0.xhu.timetable.config.store.GlobalConfigStore
import vip.mystery0.xhu.timetable.config.store.getConfigStore
import vip.mystery0.xhu.timetable.config.store.setConfigStore
import vip.mystery0.xhu.timetable.model.CustomUi
import vip.mystery0.xhu.timetable.model.WeekCourseView
import vip.mystery0.xhu.timetable.model.event.EventType
import vip.mystery0.xhu.timetable.repository.local.AggregationLocalRepo
import vip.mystery0.xhu.timetable.utils.now

class CustomUiViewModel : ComposeViewModel() {
    private val _randomWeekCourse = MutableStateFlow<List<WeekCourseView>>(emptyList())
    val randomWeekCourse: StateFlow<List<WeekCourseView>> = _randomWeekCourse

    private val _randomTodayCourse = MutableStateFlow<List<TodayCourseSheet>>(emptyList())
    val randomTodayCourse: StateFlow<List<TodayCourseSheet>> = _randomTodayCourse

    private val _customUi = MutableStateFlow(GlobalConfigStore.customUi)
    val customUi: StateFlow<CustomUi> = _customUi

    val weekItemHeight = MutableStateFlow(CustomUi.DEFAULT.weekItemHeight)
    val todayBackgroundAlpha = MutableStateFlow(CustomUi.DEFAULT.todayBackgroundAlpha)
    val weekBackgroundAlpha = MutableStateFlow(CustomUi.DEFAULT.weekBackgroundAlpha)
    val weekItemCorner = MutableStateFlow(CustomUi.DEFAULT.weekItemCorner)
    val weekTitleTemplate = MutableStateFlow(CustomUi.DEFAULT.weekTitleTemplate)
    val weekNotTitleTemplate = MutableStateFlow(CustomUi.DEFAULT.weekNotTitleTemplate)
    val weekTitleTextSize = MutableStateFlow(CustomUi.DEFAULT.weekTitleTextSize)
    val backgroundImageBlur = MutableStateFlow(CustomUi.DEFAULT.backgroundImageBlur)

    fun init() {
        loadCustomUi()
        refreshRandomCourse()
    }

    private fun loadCustomUi() {
        viewModelScope.safeLaunch {
            val customUi = getConfigStore { customUi }
            _customUi.value = customUi
            weekItemHeight.value = customUi.weekItemHeight
            todayBackgroundAlpha.value = customUi.todayBackgroundAlpha
            weekBackgroundAlpha.value = customUi.weekBackgroundAlpha
            weekItemCorner.value = customUi.weekItemCorner
            weekTitleTemplate.value = customUi.weekTitleTemplate
            weekNotTitleTemplate.value = customUi.weekNotTitleTemplate
            weekTitleTextSize.value = customUi.weekTitleTextSize
            backgroundImageBlur.value = customUi.backgroundImageBlur
        }
    }

    fun refreshRandomCourse() {
        viewModelScope.safeLaunch {
            val (weekList, todayList) = AggregationLocalRepo.getRandomCourseList(
                weekSize = 6,
                todaySize = 2
            )
            val today = LocalDate.now()
            val now = LocalDateTime.now()
            _randomWeekCourse.value = weekList
            _randomTodayCourse.value = todayList.map {
                it.updateTime()
                TodayCourseSheet(
                    it.courseName,
                    it.teacher,
                    (it.startDayTime..it.endDayTime).sorted().toMutableSet(),
                    it.startTime to it.endTime,
                    it.courseDayTime,
                    it.location,
                    it.backgroundColor,
                    today,
                    "",
                ).calc(now)
            }
        }
    }

    fun reset() {
        viewModelScope.safeLaunch {
            setConfigStore { customUi = CustomUi.DEFAULT }
            loadCustomUi()
        }
    }

    fun update() {
        viewModelScope.safeLaunch {
            val nowCustomUi = CustomUi(
                todayBackgroundAlpha.value,
                weekItemHeight.value,
                weekBackgroundAlpha.value,
                weekItemCorner.value,
                weekTitleTemplate.value.ifEmpty { CustomUi.DEFAULT.weekTitleTemplate },
                weekNotTitleTemplate.value.ifEmpty { CustomUi.DEFAULT.weekNotTitleTemplate },
                weekTitleTextSize.value,
                backgroundImageBlur.value,
            )
            _customUi.value = nowCustomUi
        }
    }

    fun save() {
        viewModelScope.safeLaunch {
            val nowCustomUi = CustomUi(
                todayBackgroundAlpha.value,
                weekItemHeight.value,
                weekBackgroundAlpha.value,
                weekItemCorner.value,
                weekTitleTemplate.value.ifEmpty { CustomUi.DEFAULT.weekTitleTemplate },
                weekNotTitleTemplate.value.ifEmpty { CustomUi.DEFAULT.weekNotTitleTemplate },
                weekTitleTextSize.value,
                backgroundImageBlur.value,
            )
            setConfigStore { customUi = nowCustomUi }
            EventBus.post(EventType.CHANGE_CUSTOM_UI)
            EventBus.post(EventType.CHANGE_MAIN_BACKGROUND)
            loadCustomUi()
        }
    }
}