package vip.mystery0.xhu.timetable.viewmodel

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import vip.mystery0.xhu.timetable.base.ComposeViewModel
import vip.mystery0.xhu.timetable.config.store.EventBus
import vip.mystery0.xhu.timetable.model.event.EventType
import vip.mystery0.xhu.timetable.repository.CourseColorRepo
import vip.mystery0.xhu.timetable.utils.toHexString

class CustomCourseColorViewModel : ComposeViewModel(), KoinComponent {
    private val _listState = MutableStateFlow<List<Pair<String, Color>>>(emptyList())
    val listState: StateFlow<List<Pair<String, Color>>> = _listState

    fun init() {
        loadList("")
    }

    fun loadList(keywords: String) {
        viewModelScope.launch {
            _listState.value = CourseColorRepo.getCourseColorList(keywords)
        }
    }

    fun updateColor(courseName: String, selectedColor: Color?) {
        viewModelScope.launch {
            if (selectedColor != null) {
                val color = selectedColor.toHexString()
                CourseColorRepo.updateCourseColor(courseName, color)
            } else {
                CourseColorRepo.updateCourseColor(courseName, null)
            }
            loadList("")
            EventBus.post(EventType.CHANGE_COURSE_COLOR)
        }
    }
}