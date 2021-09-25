package vip.mystery0.xhu.timetable.viewmodel

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import vip.mystery0.xhu.timetable.api.PoemsApi
import vip.mystery0.xhu.timetable.api.ServerApi
import vip.mystery0.xhu.timetable.base.ComposeViewModel
import vip.mystery0.xhu.timetable.config.Config
import vip.mystery0.xhu.timetable.config.chinaZone
import vip.mystery0.xhu.timetable.model.response.Poems
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.TextStyle
import java.util.*

class MainViewModel : ComposeViewModel(), KoinComponent {
    companion object {
        private const val TAG = "LoginViewModel"
    }

    private val serverApi: ServerApi by inject()
    private val poemsApi: PoemsApi by inject()

    private val _todayTitle = MutableStateFlow("")
    val todayTitle: StateFlow<String> = _todayTitle

    private val _week = MutableStateFlow(1)
    val week: StateFlow<Int> = _week

    private val _poems = MutableStateFlow<Poems?>(null)
    val poems: StateFlow<Poems?> = _poems

    init {
        calculateTodayTitle()
        calculateWeek()
        showPoems()
    }

    private fun calculateTodayTitle() {
        viewModelScope.launch {
            val nowDate = LocalDate.now()
            val todayWeekIndex = nowDate.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.CHINESE)
            val startDate = LocalDateTime.ofInstant(Config.termStartTime, chinaZone).toLocalDate()
            if (nowDate.isBefore(startDate)) {
                //开学之前，那么计算剩余时间
                val remainDays =
                    Duration.between(nowDate.atStartOfDay(), startDate.atStartOfDay()).toDays()
                _todayTitle.value = "距离开学还有${remainDays}天 $todayWeekIndex"
            } else {
                val days =
                    Duration.between(startDate.atStartOfDay(), nowDate.atStartOfDay()).toDays()
                val week = (days / 7) + 1
                _todayTitle.value = "第${week}周 $todayWeekIndex"
            }
        }
    }

    private fun calculateWeek() {
        viewModelScope.launch {
            val nowDate = LocalDate.now()
            _week.value = nowDate.dayOfWeek.value
        }
    }

    private fun showPoems() {
        viewModelScope.launch {
            val token = Config.poemsToken
            if (token == null) {
                Config.poemsToken = poemsApi.getToken().data
            }
            _poems.value = poemsApi.getSentence().data
        }
    }
}