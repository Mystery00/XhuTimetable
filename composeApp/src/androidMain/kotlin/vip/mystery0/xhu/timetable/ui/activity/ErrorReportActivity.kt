package vip.mystery0.xhu.timetable.ui.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.compose.rememberShareFileLauncher
import io.github.vinceglb.filekit.exists
import io.github.vinceglb.filekit.manualFileKitCoreInitialization
import vip.mystery0.xhu.timetable.base.appVersionCode
import vip.mystery0.xhu.timetable.base.appVersionName
import vip.mystery0.xhu.timetable.config.toast.showLongToast
import vip.mystery0.xhu.timetable.ui.theme.XhuTimetableTheme
import kotlin.system.exitProcess

class ErrorReportActivity : ComponentActivity() {
    companion object {
        const val EXTRA_LOG_FILE = "EXTRA_LOG_FILE"
        const val EXTRA_STACK_TRACE = "EXTRA_STACK_TRACE"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        FileKit.manualFileKitCoreInitialization(this)
        setContent {
            XhuTimetableTheme {
                BuildContent()
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun BuildContent() {
        val launcher = rememberShareFileLauncher()
        val uriHandler = LocalUriHandler.current

        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text(text = "错误报告") },
                )
            },
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .padding(paddingValues)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1F)
                        .verticalScroll(state = rememberScrollState()),
                ) {
                    Text(
                        text = "Version: ${appVersionName()}(${appVersionCode()})",
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
                        onClick = {
                            uriHandler.openUri("https://blog.mystery0.vip/xgkb-group")
                        },
                        modifier = Modifier.weight(1F),
                    ) {
                        Text(text = "加入交流反馈群")
                    }
                    Button(
                        onClick = {
                            val logFilePath = intent.getStringExtra(EXTRA_LOG_FILE)
                            if (logFilePath.isNullOrBlank()) {
                                showLongToast("日志文件不存在")
                                return@Button
                            }
                            val logFile = PlatformFile(logFilePath)
                            if (!logFile.exists()) {
                                showLongToast("日志文件不存在")
                                return@Button
                            }
                            launcher.launch(logFile)
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

    private fun killCurrentProcess() {
        finish()
        android.os.Process.killProcess(android.os.Process.myPid())
        exitProcess(10)
    }
}