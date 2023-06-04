package vip.mystery0.xhu.timetable.viewmodel

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import vip.mystery0.xhu.timetable.base.ComposeViewModel
import vip.mystery0.xhu.timetable.config.store.EventBus
import vip.mystery0.xhu.timetable.config.store.getConfigStore
import vip.mystery0.xhu.timetable.config.store.setConfigStore
import vip.mystery0.xhu.timetable.model.CustomUi
import vip.mystery0.xhu.timetable.model.WeekCourseView
import vip.mystery0.xhu.timetable.model.event.EventType
import vip.mystery0.xhu.timetable.repository.local.AggregationLocalRepo

class CustomUiViewModel : ComposeViewModel() {
    private val _randomCourse = MutableStateFlow<List<WeekCourseView>>(emptyList())
    val randomCourse: StateFlow<List<WeekCourseView>> = _randomCourse

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
            val customUi = getConfigStore { customUi }
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
            _randomCourse.value = AggregationLocalRepo.getRandomCourseList(6)
        }
    }

    fun reset() {
        viewModelScope.launch {
            setConfigStore { customUi = CustomUi.DEFAULT }
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
            setConfigStore { customUi = nowCustomUi }
            EventBus.post(EventType.CHANGE_CUSTOM_UI)
            EventBus.post(EventType.CHANGE_MAIN_BACKGROUND)
            loadCustomUi()
        }
    }
}