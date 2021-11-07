package vip.mystery0.xhu.timetable.viewmodel

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import vip.mystery0.xhu.timetable.base.ComposeViewModel
import vip.mystery0.xhu.timetable.model.event.EventType
import vip.mystery0.xhu.timetable.model.event.UIEvent
import vip.mystery0.xhu.timetable.repository.getCourseColorList
import vip.mystery0.xhu.timetable.repository.updateCourseColor
import java.text.Collator
import java.util.*

class CustomCourseColorViewModel : ComposeViewModel(), KoinComponent {
    companion object {
        private val comparator = Collator.getInstance(Locale.CHINA)
    }

    private val eventBus: EventBus by inject()

    private val _listState = MutableStateFlow<List<Pair<String, Color>>>(emptyList())
    val listState: StateFlow<List<Pair<String, Color>>> = _listState

    init {
        viewModelScope.launch {
            _listState.value = getCourseColorList()
                .sortedWith { o1, o2 -> comparator.compare(o1.first, o2.first) }
        }
    }

    fun updateColor(courseName: String, selectedColor: Color?) {
        viewModelScope.launch {
            if (selectedColor != null) {
                val color = toColorString(selectedColor)
                updateCourseColor(courseName, color)
            } else {
                updateCourseColor(courseName, null)
            }
            _listState.value = getCourseColorList()
                .sortedWith { o1, o2 -> comparator.compare(o1.first, o2.first) }
            eventBus.post(UIEvent(EventType.CHANGE_TERM_START_TIME))
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