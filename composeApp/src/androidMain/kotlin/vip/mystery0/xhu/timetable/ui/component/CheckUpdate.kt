package vip.mystery0.xhu.timetable.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
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
import vip.mystery0.xhu.timetable.config.coroutine.safeLaunch
import vip.mystery0.xhu.timetable.config.store.GlobalCacheStore
import vip.mystery0.xhu.timetable.config.store.GlobalConfigStore
import vip.mystery0.xhu.timetable.model.response.ClientVersion
import vip.mystery0.xhu.timetable.utils.forceExit
import vip.mystery0.xhu.timetable.utils.formatFileSize

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

private val apkDownloadObserver = HashMap<Long, DownloadObserver>()
private val patchDownloadObserver = HashMap<Long, DownloadObserver>()

private val coroutineScope = CoroutineScope(Dispatchers.Default)

fun addDownloadObserver(
    patchObserver: Boolean = true,
    listener: suspend (DownloadUpdateState) -> Unit
): Long = if (patchObserver) {
    val lastKey = if (patchDownloadObserver.isEmpty()) 0L else patchDownloadObserver.keys.max()
    val key = lastKey + 1
    patchDownloadObserver[key] = listener
    key
} else {
    val lastKey = if (patchDownloadObserver.isEmpty()) 0L else patchDownloadObserver.keys.max()
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

fun updateObserverProgress(state: DownloadUpdateState) {
    coroutineScope.safeLaunch {
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
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(text = version.updateLog)
                if (downloadProgress.downloading) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        LinearProgressIndicator(
                            progress = { downloadProgress.progress / 100F },
                            modifier = Modifier.weight(1F),
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${downloadProgress.progress}%",
                            modifier = Modifier.width(64.dp),
                            textAlign = TextAlign.Center,
                        )
                    }
                }
                if (downloadProgress.status.isNotBlank()) {
                    Text(
                        text = downloadProgress.status,
                    )
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
                    forceExit()
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