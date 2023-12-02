package vip.mystery0.xhu.timetable.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue

@Composable
fun rememberXhuDialogState(): XhuDialogState =
    rememberSaveable(saver = XhuDialogState.Saver()) {
        XhuDialogState()
    }

class XhuDialogState(initialValue: Boolean = false) {
    var showing by mutableStateOf(initialValue)
        private set

    fun show() {
        showing = true
    }

    fun hide() {
        showing = false
    }

    companion object {
        fun Saver(): Saver<XhuDialogState, Boolean> = Saver(
            save = { it.showing },
            restore = { XhuDialogState(it) }
        )
    }
}