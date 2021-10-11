package vip.mystery0.xhu.timetable.viewmodel

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.greenrobot.eventbus.EventBus
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import vip.mystery0.xhu.timetable.base.ComposeViewModel

class ClassSettingsViewModel : ComposeViewModel(), KoinComponent {
    private val eventBus: EventBus by inject()

    private val _errorMessage = MutableStateFlow("")
    val errorMessage: StateFlow<String> = _errorMessage
}