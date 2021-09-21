package vip.mystery0.xhu.timetable.viewmodel

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.koin.core.component.KoinComponent
import vip.mystery0.xhu.timetable.base.ComposeViewModel
import vip.mystery0.xhu.timetable.config.DataHolder

class LoginViewModel : ComposeViewModel(), KoinComponent {
    private val _updateDialogState = MutableStateFlow(false)
    val updateDialogState: StateFlow<Boolean> = _updateDialogState

    init {
        val newVersionCode = DataHolder.version?.versionCode ?: 0L
//            _updateDialogState.value = newVersionCode <= appVersionCodeNumber
        _updateDialogState.value = true
    }

    fun closeUpdateDialog() {
        _updateDialogState.value = false
    }
}