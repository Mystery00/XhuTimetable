package vip.mystery0.xhu.timetable.viewmodel

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.koin.core.component.inject
import vip.mystery0.xhu.timetable.base.ComposeViewModel
import vip.mystery0.xhu.timetable.config.getConfig
import vip.mystery0.xhu.timetable.config.setConfig
import vip.mystery0.xhu.timetable.model.Course
import vip.mystery0.xhu.timetable.model.CustomUi
import vip.mystery0.xhu.timetable.model.entity.CourseType
import vip.mystery0.xhu.timetable.model.event.EventType
import vip.mystery0.xhu.timetable.model.event.UIEvent
import vip.mystery0.xhu.timetable.module.localRepo
import vip.mystery0.xhu.timetable.repository.CourseRepo
import vip.mystery0.xhu.timetable.ui.theme.ColorPool
import vip.mystery0.xhu.timetable.ui.theme.XhuColor
import kotlin.random.Random

class CustomUiViewModel : ComposeViewModel() {
    private val eventBus: EventBus by inject()
    private val courseLocalRepo: CourseRepo by localRepo()

    private val _randomCourse = MutableStateFlow<List<Course>>(emptyList())
    val randomCourse: StateFlow<List<Course>> = _randomCourse

    private val _customUi = MutableStateFlow(CustomUi.DEFAULT)
    val customUi: StateFlow<CustomUi> = _customUi

    val weekItemHeight = MutableStateFlow(CustomUi.DEFAULT.weekItemHeight)
    val weekBackgroundAlpha = MutableStateFlow(CustomUi.DEFAULT.weekBackgroundAlpha)
    val weekItemCorner = MutableStateFlow(CustomUi.DEFAULT.weekItemCorner)
    val weekTitleTemplate = MutableStateFlow(CustomUi.DEFAULT.weekTitleTemplate)
    val weekNotTitleTemplate = MutableStateFlow(CustomUi.DEFAULT.weekNotTitleTemplate)
    val weekTitleTextSize = MutableStateFlow(CustomUi.DEFAULT.weekTitleTextSize)
    val backgroundImageBlur = MutableStateFlow(CustomUi.DEFAULT.backgroundImageBlur)

    init {
        loadCustomUi()
        refreshRandomCourse()
    }

    private fun loadCustomUi() {
        viewModelScope.launch {
            val customUi = getConfig { customUi }
            _customUi.value = customUi
            weekItemHeight.value = customUi.weekItemHeight
            weekBackgroundAlpha.value = customUi.weekBackgroundAlpha
            weekItemCorner.value = customUi.weekItemCorner
            weekTitleTemplate.value = customUi.weekTitleTemplate
            weekNotTitleTemplate.value = customUi.weekNotTitleTemplate
            weekTitleTextSize.value = customUi.weekTitleTextSize
            backgroundImageBlur.value = customUi.backgroundImageBlur
        }
    }

    fun refreshRandomCourse() {
        viewModelScope.launch {
            val courseList = courseLocalRepo.getRandomCourseList(6)
            _randomCourse.value = courseList.map {
                val thisWeek = Random.nextBoolean()
                Course(
                    courseName = it.name,
                    teacherName = it.teacher,
                    location = it.location,
                    weekSet = emptyList(),
                    weekString = "",
                    type = CourseType.ALL,
                    timeSet = emptyList(),
                    timeString = "",
                    time = "",
                    day = it.day,
                    extraData = it.extraData,
                    thisWeek = thisWeek,
                    today = false,
                    tomorrow = false,
                    color = if (thisWeek) ColorPool.hash(it.name) else XhuColor.notThisWeekBackgroundColor,
                    studentId = "",
                    userName = "",
                )
            }
        }
    }

    fun reset() {
        viewModelScope.launch {
            setConfig { customUi = CustomUi.DEFAULT }
            loadCustomUi()
        }
    }

    fun update() {
        viewModelScope.launch {
            val nowCustomUi = CustomUi(
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
        viewModelScope.launch {
            val nowCustomUi = CustomUi(
                weekItemHeight.value,
                weekBackgroundAlpha.value,
                weekItemCorner.value,
                weekTitleTemplate.value.ifEmpty { CustomUi.DEFAULT.weekTitleTemplate },
                weekNotTitleTemplate.value.ifEmpty { CustomUi.DEFAULT.weekNotTitleTemplate },
                weekTitleTextSize.value,
                backgroundImageBlur.value,
            )
            setConfig { customUi = nowCustomUi }
            eventBus.post(UIEvent(EventType.CHANGE_CUSTOM_UI))
            eventBus.post(UIEvent(EventType.CHANGE_MAIN_BACKGROUND))
            loadCustomUi()
        }
    }
}