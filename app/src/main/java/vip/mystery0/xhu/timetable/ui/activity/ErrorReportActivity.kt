package vip.mystery0.xhu.timetable.ui.activity

import android.os.Bundle
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import vip.mystery0.xhu.timetable.appName
import vip.mystery0.xhu.timetable.appVersionCode
import vip.mystery0.xhu.timetable.appVersionName
import vip.mystery0.xhu.timetable.base.BaseComposeActivity
import vip.mystery0.xhu.timetable.joinQQGroup
import kotlin.system.exitProcess

class ErrorReportActivity : BaseComposeActivity() {
    companion object {
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