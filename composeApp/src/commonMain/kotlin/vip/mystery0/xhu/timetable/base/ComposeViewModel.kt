package vip.mystery0.xhu.timetable.base

import androidx.lifecycle.ViewModel
import co.touchlab.kermit.Logger
import kotlinx.coroutines.flow.MutableStateFlow
import org.koin.core.component.KoinComponent

abstract class ComposeViewModel : ViewModel(), KoinComponent {
    val logger = Logger.withTag(this::class.simpleName!!)
    val errorMessage = MutableStateFlow("")

    protected fun toastMessage(message: String) {
        errorMessage.value = message
    }
}