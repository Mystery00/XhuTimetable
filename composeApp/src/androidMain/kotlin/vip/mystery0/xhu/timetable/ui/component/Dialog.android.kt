package vip.mystery0.xhu.timetable.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import org.koin.compose.viewmodel.koinViewModel
import vip.mystery0.xhu.timetable.config.coroutine.safeLaunch
import vip.mystery0.xhu.timetable.model.response.ClientVersion
import vip.mystery0.xhu.timetable.repository.StartRepo
import vip.mystery0.xhu.timetable.viewmodel.CheckUpdateVewModel

@Composable
actual fun ShowUpdateDialog() {
    val viewModel = koinViewModel<CheckUpdateVewModel>()

    val version by viewModel.version.collectAsState()

    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.init()
    }
    val newVersion = version ?: return
    if (newVersion == ClientVersion.EMPTY) {
        return
    }
    //需要提示版本更新
    CheckUpdate(
        version = newVersion,
        onDownload = {
            if (it) {
                viewModel.downloadApk()
            } else {
                viewModel.downloadPatch()
            }
        },
        onIgnore = {
            viewModel.ignoreVersion()
        },
        onClose = {
            scope.safeLaunch {
                StartRepo.version.emit(ClientVersion.EMPTY)
            }
        },
    )
}