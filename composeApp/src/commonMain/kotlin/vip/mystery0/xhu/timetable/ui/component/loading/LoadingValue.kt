package vip.mystery0.xhu.timetable.ui.component.loading

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember

enum class LoadingValue {
    Loading, Stop,
}

@Composable
fun rememberLoadingState(initState: LoadingValue = LoadingValue.Stop): MutableState<LoadingValue> =
    remember {
        mutableStateOf(initState)
    }
