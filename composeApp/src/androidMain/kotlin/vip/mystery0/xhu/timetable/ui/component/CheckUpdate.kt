package vip.mystery0.xhu.timetable.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import vip.mystery0.xhu.timetable.config.coroutine.safeLaunch
import vip.mystery0.xhu.timetable.config.store.GlobalCacheStore
import vip.mystery0.xhu.timetable.config.store.GlobalConfigStore
import vip.mystery0.xhu.timetable.model.response.ClientVersion
import vip.mystery0.xhu.timetable.utils.forceExit
import vip.mystery0.xhu.timetable.utils.formatFileSize
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

data class DownloadUpdateState(
    val downloading: Boolean = false,
    val indeterminate: Boolean = false,
    val totalSize: Long = 0L,
    val downloaded: Long = 0L,
    val patch: Boolean = false,
    val progress: Int = 0,
    val status: String = "",
    val failed: Boolean = false,
)

typealias DownloadObserver = suspend (DownloadUpdateState) -> Unit

private val downloadStateFlow = MutableStateFlow(DownloadUpdateState())

private val observerIdGenerator = AtomicLong(0L)
private val apkDownloadObserver = ConcurrentHashMap<Long, DownloadObserver>()
private val patchDownloadObserver = ConcurrentHashMap<Long, DownloadObserver>()

private val coroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

fun addDownloadObserver(
    patchObserver: Boolean = true,
    listener: suspend (DownloadUpdateState) -> Unit
): Long {
    val key = observerIdGenerator.incrementAndGet()
    if (patchObserver) {
        patchDownloadObserver[key] = listener
    } else {
        apkDownloadObserver[key] = listener
    }
    return key
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

fun markDownloadStarting(patch: Boolean) {
    downloadStateFlow.value = DownloadUpdateState(
        downloading = true,
        indeterminate = true,
        patch = patch,
        progress = 0,
        status = "正在准备下载...",
        failed = false,
    )
}

fun resetDownloadState() {
    downloadStateFlow.value = DownloadUpdateState()
}

fun updateObserverProgress(state: DownloadUpdateState) {
    // 同步保序更新 UI 状态，避免并发协程乱序覆盖进度
    downloadStateFlow.value = state
    val observers = if (state.patch) {
        patchDownloadObserver.values.toList()
    } else {
        apkDownloadObserver.values.toList()
    }
    if (observers.isNotEmpty()) {
        coroutineScope.safeLaunch {
            observers.forEach { it(state) }
        }
    }
}

fun updateStatus(
    status: String,
    patch: Boolean = false,
    progress: Int = 0,
    downloading: Boolean = true,
    indeterminate: Boolean = progress <= 0 && downloading,
    failed: Boolean = false,
) {
    val current = downloadStateFlow.value
    downloadStateFlow.value = DownloadUpdateState(
        downloading = downloading,
        indeterminate = indeterminate,
        totalSize = current.totalSize,
        downloaded = current.downloaded,
        patch = patch,
        progress = progress.coerceIn(0, 100),
        status = status,
        failed = failed,
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
    var dialogState by remember(version.versionId, version.versionCode) { mutableStateOf(true) }
    val downloadProgress by downloadStateFlow.collectAsState()
    val onCloseListener = { skip: Boolean ->
        if (skip) {
            when {
                GlobalConfigStore.debugMode && GlobalCacheStore.alwaysShowNewVersion -> {
                    dialogState = false
                    if (!downloadProgress.downloading) {
                        resetDownloadState()
                    }
                    onClose()
                }

                !version.forceUpdate -> {
                    dialogState = false
                    if (!downloadProgress.downloading) {
                        resetDownloadState()
                    }
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
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(text = version.updateLog)
                if (downloadProgress.downloading || downloadProgress.status.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                }
                if (downloadProgress.downloading) {
                    if (downloadProgress.indeterminate) {
                        LinearProgressIndicator(
                            modifier = Modifier.fillMaxWidth(),
                        )
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            LinearProgressIndicator(
                                progress = { (downloadProgress.progress / 100F).coerceIn(0F, 1F) },
                                modifier = Modifier.weight(1F),
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "${downloadProgress.progress}%",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontFeatureSettings = "tnum",
                                ),
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.width(44.dp),
                                textAlign = TextAlign.End,
                            )
                        }
                    }
                }
                if (downloadProgress.status.isNotBlank()) {
                    Text(
                        text = downloadProgress.status,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (downloadProgress.failed) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                    )
                }
            }
        },
        confirmButton = {
            if (version.showPatch) {
                TextButton(
                    enabled = !downloadProgress.downloading,
                    onClick = {
                        markDownloadStarting(patch = true)
                        onDownload(false)
                        onCloseListener(false)
                    },
                ) {
                    Text(text = "下载增量包(${version.patchSize.formatFileSize()})")
                }
            }
            TextButton(
                enabled = !downloadProgress.downloading,
                onClick = {
                    markDownloadStarting(patch = false)
                    onDownload(true)
                    onCloseListener(false)
                },
            ) {
                Text(text = "下载APK(${version.apkSize.formatFileSize()})")
            }
        },
        dismissButton = {
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
        },
    )
}
