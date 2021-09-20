package vip.mystery0.xhu.timetable.viewmodel

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import vip.mystery0.xhu.timetable.base.ComposeViewModel
import vip.mystery0.xhu.timetable.model.response.InitResponse
import vip.mystery0.xhu.timetable.module.repo
import vip.mystery0.xhu.timetable.repository.StartRepo

class StarterViewModel : ComposeViewModel(), KoinComponent {
    private val startRepo: StartRepo by repo()

    private val _readyState = MutableStateFlow(ReadyState(loading = true))
    val readyState: StateFlow<ReadyState> = _readyState

    init {
        viewModelScope.launch {
            _readyState.value = ReadyState(response = startRepo.init())
        }
    }
}

data class ReadyState(
    val loading: Boolean = false,
    val response: InitResponse? = null,
)