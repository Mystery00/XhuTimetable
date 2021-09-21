package vip.mystery0.xhu.timetable.ui.activity

import androidx.compose.material.AlertDialog
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.res.stringResource
import com.google.accompanist.flowlayout.FlowRow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import vip.mystery0.xhu.timetable.R
import vip.mystery0.xhu.timetable.appVersionName
import vip.mystery0.xhu.timetable.model.response.Version
import java.text.DecimalFormat
import java.util.*

data class DownloadUpdateState(
    val downloading: Boolean = false,
    val totalSize: Long = 0L,
    val downloaded: Long = 0L,
    val patch: Boolean = false,
) {
    val progress: Int = (downloaded * 100 / totalSize).toInt()
}

typealias DownloadObserver = (DownloadUpdateState) -> Unit

private val downloadStateFlow = MutableStateFlow(DownloadUpdateState())

private val apkDownloadObserver = TreeMap<Long, DownloadObserver>()
private val patchDownloadObserver = TreeMap<Long, DownloadObserver>()

fun addDownloadObserver(
    patchObserver: Boolean = true,
    listener: (DownloadUpdateState) -> Unit
): Long =
    if (patchObserver) {
        val key = patchDownloadObserver.lastKey() + 1
        patchDownloadObserver[key] = listener
        key
    } else {
        val key = apkDownloadObserver.lastKey() + 1
        apkDownloadObserver[key] = listener
        key
    }

fun removeDownloadObserver(
    patchObserver: Boolean = true,
    observerId: Long,
) {
    if (patchObserver) {
        patchDownloadObserver.remove(observerId)
    } else {
        apkDownloadObserver.remove(observerId)
    }
}

suspend fun updateProgress(state: DownloadUpdateState) {
    downloadStateFlow.emit(state)
}


@Composable
fun CheckUpdate(
    versionStateFlow: StateFlow<Version?>,
    onCloseListener: () -> Unit,
    onDownload: (Boolean) -> Unit,
    onIgnore: () -> Unit,
) {
    val versionState = versionStateFlow.collectAsState()
    versionState.value?.let { version ->
        AlertDialog(onDismissRequest = onCloseListener,
            title = {
                Text(text = stringResource(R.string.dialog_update_title))
            }, text = {
                Text(
                    text = stringResource(
                        R.string.dialog_update_text,
                        version.versionName,
                        appVersionName,
                        version.updateLog
                    )
                )
            }, confirmButton = {
                FlowRow {
                    TextButton(onClick = {
                        onDownload(false)
                        onCloseListener()
                    }) {
                        Text(text = stringResource(id = R.string.action_download_patch))
                    }
                    TextButton(onClick = {
                        onDownload(true)
                        onCloseListener()
                    }) {
                        Text(text = stringResource(id = R.string.action_download_apk))
                    }
                }
            }, dismissButton = {
                TextButton(onClick = {
                    onIgnore()
                    onCloseListener()
                }) {
                    Text(text = stringResource(id = R.string.action_download_cancel))
                }
            })
    }
}

fun Long.formatFileSize(decimalNum: Int = 2): String {
    if (this <= 0L)
        return "0B"
    val formatString = StringBuilder()
    formatString.append("#.")
    if (decimalNum <= 0)
        formatString.deleteCharAt(1)
    else
        for (i in 0 until decimalNum)
            formatString.append('0')
    val decimalFormat = DecimalFormat(formatString.toString())
    return when {
        this < 1024 -> decimalFormat.format(this) + 'B'
        this < 1024 * 1024 -> decimalFormat.format(this.toFloat() / 1024f) + "KB"
        this < 1024 * 1024 * 1024 -> decimalFormat.format(this.toFloat() / 1024f / 1024f) + "MB"
        else -> decimalFormat.format(this.toFloat() / 1024f / 1024f / 1024f) + "GB"
    }
}