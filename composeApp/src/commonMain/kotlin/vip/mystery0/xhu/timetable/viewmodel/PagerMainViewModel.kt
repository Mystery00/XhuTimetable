package vip.mystery0.xhu.timetable.viewmodel

import com.maxkeppeker.sheets.core.models.base.UseCaseState
import kotlinx.coroutines.flow.MutableStateFlow
import vip.mystery0.xhu.timetable.base.ComposeViewModel

class PagerMainViewModel : ComposeViewModel() {
    val showAddDialog = MutableStateFlow(UseCaseState())
    val showWeekViewDialog = MutableStateFlow(UseCaseState())
}