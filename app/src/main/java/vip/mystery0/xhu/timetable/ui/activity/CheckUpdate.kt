package vip.mystery0.xhu.timetable.ui.activity

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import vip.mystery0.xhu.timetable.config.store.GlobalCacheStore
import vip.mystery0.xhu.timetable.config.store.GlobalConfigStore
import vip.mystery0.xhu.timetable.model.response.ClientVersion
import vip.mystery0.xhu.timetable.utils.finishAllActivity
import java.text.DecimalFormat
import java.util.TreeMap

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

suspend fun updateStatus(
    status: String,
    patch: Boolean = false,
    progress: Int = 0,
) {
    downloadStateFlow.emit(
        DownloadUpdateState(
            downloading = true,
            patch = patch,
            progress = progress,
            status = status,
        )
    )
}


@Composable
fun CheckUpdate(
    version: ClientVersion?,
    onDownload: (Boolean) -> Unit,
    onIgnore: () -> Unit,
    onClose: () -> Unit,
) {
    if (version == null) {
        return
    }
    var dialogState by remember { mutableStateOf(true) }
    val downloadProgress by downloadStateFlow.collectAsState()
    val onCloseListener = { skip: Boolean ->
        if (skip) {
            when {
                GlobalConfigStore.debugMode && GlobalCacheStore.alwaysShowNewVersion -> {
                    dialogState = false
                    onClose()
                }

                !version.forceUpdate -> {
                    dialogState = false
                    onClose()
                }
            }
        }
    }
    if (!dialogState) return
    AlertDialog(
        onDismissRequest = {
            onCloseListener(true)
        },
        title = {
            Text(
                text = "检测到新版本 ${version.versionName}",
                fontWeight = FontWeight.W700,
                style = MaterialTheme.typography.titleLarge,
            )
        }, text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(text = version.updateLog)
                Row(
                    modifier = Modifier.height(48.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (downloadProgress.downloading) {
                        LinearProgressIndicator(
                            progress = downloadProgress.progress / 100F,
                            modifier = Modifier.weight(1F),
                        )
                        Text(
                            text = downloadProgress.status,
                            modifier = Modifier.weight(0.2F),
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }
        }, confirmButton = {
            if (version.showPatch) {
                TextButton(onClick = {
                    onDownload(false)
                    onCloseListener(false)
                }) {
                    Text(text = "下载增量包(${version.patchSize.formatFileSize()})")
                }
            }
            TextButton(onClick = {
                onDownload(true)
                onCloseListener(false)
            }) {
                Text(text = "下载APK(${version.apkSize.formatFileSize()})")
            }
        }, dismissButton = {
            if (version.forceUpdate) {
                TextButton(onClick = {
                    finishAllActivity()
                }) {
                    Text(text = "退出应用")
                }
            } else {
                TextButton(onClick = {
                    onIgnore()
                    onCloseListener(true)
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