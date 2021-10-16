package vip.mystery0.xhu.timetable.ui.activity

import androidx.compose.material.AlertDialog
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.*
import com.google.accompanist.flowlayout.FlowRow
import kotlinx.coroutines.flow.MutableStateFlow
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
) {
    val progress: Int
        get() = (downloaded * 100 / totalSize).toInt()
}

typealias DownloadObserver = suspend (DownloadUpdateState) -> Unit

private val downloadStateFlow = MutableStateFlow(DownloadUpdateState())

private val apkDownloadObserver = TreeMap<Long, DownloadObserver>()
private val patchDownloadObserver = TreeMap<Long, DownloadObserver>()

suspend fun addDownloadObserver(
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

suspend fun updateProgress(state: DownloadUpdateState) {
    downloadStateFlow.emit(state)
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
    AlertDialog(onDismissRequest = onCloseListener,
        title = {
            Text(text = "检测到新版本")
        }, text = {
            Text(
                text = """
                新版本：${version.versionName}
                当前版本：${appVersionName}
                更新日志：
                ${version.updateLog}
            """.trimIndent()
            )
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