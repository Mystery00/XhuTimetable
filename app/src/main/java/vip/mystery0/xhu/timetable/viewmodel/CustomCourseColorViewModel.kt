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
import java.text.Collator
import java.util.Locale

class CustomCourseColorViewModel : ComposeViewModel(), KoinComponent {
    companion object {
        private val comparator = Collator.getInstance(Locale.CHINA)
    }

    private val _listState = MutableStateFlow<List<Pair<String, Color>>>(emptyList())
    val listState: StateFlow<List<Pair<String, Color>>> = _listState

    init {
        loadList("")
    }

    fun loadList(keywords: String) {
        viewModelScope.launch {
            _listState.value = CourseColorRepo.getCourseColorList(keywords)
                .sortedWith { o1, o2 -> comparator.compare(o1.first, o2.first) }
        }
    }

    fun updateColor(courseName: String, selectedColor: Color?) {
        viewModelScope.launch {
            if (selectedColor != null) {
                val color = toColorString(selectedColor)
                CourseColorRepo.updateCourseColor(courseName, color)
            } else {
                CourseColorRepo.updateCourseColor(courseName, null)
            }
            loadList("")
            EventBus.post(EventType.CHANGE_COURSE_COLOR)
        }
    }
}

private fun toColorString(
    color: Color,
    locale: Locale = Locale.CHINA
): String {
    val convert = android.graphics.Color.valueOf(color.red, color.green, color.blue)
    return "#${Integer.toHexString(convert.toArgb()).uppercase(locale)}"
}