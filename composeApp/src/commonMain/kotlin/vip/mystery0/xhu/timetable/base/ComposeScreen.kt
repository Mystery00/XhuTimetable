package vip.mystery0.xhu.timetable.base

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import kotlinx.coroutines.flow.MutableStateFlow
import multiplatform.network.cmptoast.showToast

@Composable
expect fun HandleBackPress(enabled: Boolean = true, backPressed: () -> Unit)

@Composable
fun HandleErrorMessage(flow: MutableStateFlow<String>) {
    val errorMessage by flow.collectAsState()
    HandleErrorMessage(errorMessage = errorMessage) {
        flow.value = ""
    }
}

@Composable
fun HandleErrorMessage(
    errorMessage: String,
    cancel: () -> Unit,
) {
    LaunchedEffect(errorMessage) {
        if (errorMessage.isNotBlank()) {
            showToast(errorMessage)
            cancel()
        }
    }
}