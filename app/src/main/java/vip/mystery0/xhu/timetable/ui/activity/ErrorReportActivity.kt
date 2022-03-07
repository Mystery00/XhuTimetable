package vip.mystery0.xhu.timetable.ui.activity

import android.content.Intent
import android.os.Bundle
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import vip.mystery0.xhu.timetable.appName
import vip.mystery0.xhu.timetable.appVersionCode
import vip.mystery0.xhu.timetable.appVersionName
import vip.mystery0.xhu.timetable.base.BaseComposeActivity
import vip.mystery0.xhu.timetable.joinQQGroup
import java.io.File
import kotlin.system.exitProcess

class ErrorReportActivity : BaseComposeActivity() {
    companion object {
        const val EXTRA_LOG_FILE = "EXTRA_LOG_FILE"
        const val EXTRA_STACK_TRACE = "EXTRA_STACK_TRACE"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        title = "$appName 错误报告"
        super.onCreate(savedInstanceState)
    }

    @Composable
    override fun BuildContent() {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(text = title.toString()) },
                    backgroundColor = MaterialTheme.colors.primary,
                    contentColor = MaterialTheme.colors.onPrimary,
                )
            },
        ) { paddingValues ->
            Column {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .padding(paddingValues)
                        .fillMaxWidth()
                        .weight(1F)
                        .verticalScroll(state = rememberScrollState()),
                ) {
                    Text(
                        text = "Version: $appVersionName($appVersionCode)",
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    val details = intent.getStringExtra(EXTRA_STACK_TRACE) ?: "未检测到异常"
                    Text(
                        text = details,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Button(
                        onClick = { joinQQGroup(this@ErrorReportActivity) },
                        modifier = Modifier.weight(1F),
                    ) {
                        Text(text = "加入交流反馈群")
                    }
                    Button(
                        onClick = {
                            val logFilePath = intent.getStringExtra(EXTRA_LOG_FILE)
                            if (logFilePath.isNullOrBlank()) {
                                toastString("日志文件不存在")
                                return@Button
                            }
                            val logFile = File(logFilePath)
                            if (!logFile.exists()) {
                                toastString("日志文件不存在")
                                return@Button
                            }
                            sendLog(logFile)
                        },
                        modifier = Modifier.weight(1F),
                    ) {
                        Text(text = "发送日志文件")
                    }
                    Button(
                        onClick = { killCurrentProcess() },
                        modifier = Modifier.weight(1F),
                    ) {
                        Text(text = "关闭应用")
                    }
                }
            }
        }
    }

    private fun sendLog(logFile: File) {
        val shareIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            putExtra(
                Intent.EXTRA_STREAM,
                FileProvider.getUriForFile(this@ErrorReportActivity, packageName, logFile)
            )
            type = "text/plain"
        }
        startActivity(Intent.createChooser(shareIntent, "发送日志到"))
    }

    private fun killCurrentProcess() {
        finish()
        android.os.Process.killProcess(android.os.Process.myPid())
        exitProcess(10)
    }
}