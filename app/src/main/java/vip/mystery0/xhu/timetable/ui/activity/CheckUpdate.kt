package vip.mystery0.xhu.timetable.ui.activity

import androidx.compose.foundation.layout.Column
import androidx.compose.material.AlertDialog
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.*
import com.google.accompanist.flowlayout.FlowRow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import vip.mystery0.xhu.timetable.appVersionCodeNumber
import vip.mystery0.xhu.timetable.appVersionName
import vip.mystery0.xhu.timetable.model.response.Version
import java.text.DecimalFormat
import java.util.*

data class DownloadUpdateState(
    val downloading: Boolean = false,
    val totalSize: Long = 0L,
    val downloaded: Long = 0L,
    val patch: Boolean = false,
    val progress: Int = 0,
    val status: String = "",
)

typealias DownloadObserver = suspend (DownloadUpdateState) -> Unit

private val downloadStateFlow = MutableStateFlow(DownloadUpdateState())

private val apkDownloadObserver = TreeMap<Long, DownloadObserver>()
private val patchDownloadObserver = TreeMap<Long, DownloadObserver>()

private val coroutineScope = CoroutineScope(Dispatchers.Default)

fun addDownloadObserver(
    patchObserver: Boolean = true,
    listener: suspend (DownloadUpdateState) -> Unit
): Long =
    if (patchObserver) {
        val lastKey = if (patchDownloadObserver.isEmpty()) 0L else patchDownloadObserver.lastKey()
        val key = lastKey + 1
        patchDownloadObserver[key] = listener
        key
    } else {
        val lastKey = if (patchDownloadObserver.isEmpty()) 0L else patchDownloadObserver.lastKey()
        val key = lastKey + 1
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

fun updateProgress(state: DownloadUpdateState) {
    coroutineScope.launch {
        downloadStateFlow.emit(state)
        if (state.patch) {
            patchDownloadObserver.values.forEach { it(state) }
        } else {
            apkDownloadObserver.values.forEach { it(state) }
        }
    }
}

fun updateStatus(
    status: String,
    downloading: Boolean = false,
    patch: Boolean = false,
    progress: Int = 0,
) {
    coroutineScope.launch {
        val state = DownloadUpdateState(
            downloading = downloading,
            patch = patch,
            progress = progress,
            status = status,
        )
        downloadStateFlow.emit(state)
    }
}


@Composable
fun CheckUpdate(
    version: Version?,
    onDownload: (Boolean) -> Unit,
    onIgnore: () -> Unit,
) {
    if (version == null) {
        return
    }
    var dialogState by remember { mutableStateOf(true) }
    val onCloseListener = {
        dialogState = false
    }
    if (!dialogState) return
    val downloadProgress by downloadStateFlow.collectAsState()
    AlertDialog(onDismissRequest = onCloseListener,
        title = {
            Text(text = "检测到新版本")
        }, text = {
            Column {
                if (downloadProgress.downloading) {
                    Text(text = downloadProgress.status)
                    LinearProgressIndicator(progress = downloadProgress.progress.toFloat())
                }
                Text(text = "新版本：${version.versionName}")
                Text(text = "当前版本：${appVersionName}")
                Text(text = "更新日志：")
                Text(text = version.updateLog)
            }
        }, confirmButton = {
            FlowRow {
                if (version.lastVersionCode == appVersionCodeNumber) {
                    TextButton(onClick = {
                        onDownload(false)
                        onCloseListener()
                    }) {
                        Text(text = "下载增量包(${version.patchSize.formatFileSize()})")
                    }
                }
                TextButton(onClick = {
                    onDownload(true)
                    onCloseListener()
                }) {
                    Text(text = "下载APK(${version.apkSize.formatFileSize()})")
                }
            }
        }, dismissButton = {
            if (!version.forceUpdate) {
                TextButton(onClick = {
                    onIgnore()
                    onCloseListener()
                }) {
                    Text(text = "忽略")
                }
            }
        })
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