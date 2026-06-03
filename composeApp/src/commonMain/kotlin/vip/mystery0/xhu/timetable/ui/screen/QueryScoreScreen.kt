package vip.mystery0.xhu.timetable.ui.screen

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.maxkeppeker.sheets.core.CoreDialog
import com.maxkeppeker.sheets.core.models.CoreSelection
import com.maxkeppeker.sheets.core.models.base.rememberUseCaseState
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel
import vip.mystery0.xhu.timetable.base.HandleErrorMessage
import vip.mystery0.xhu.timetable.model.response.ScoreGpaResponse
import vip.mystery0.xhu.timetable.model.response.ScoreResponse
import vip.mystery0.xhu.timetable.ui.component.BuildPaging
import vip.mystery0.xhu.timetable.ui.component.BuildSelectFilterChipContent
import vip.mystery0.xhu.timetable.ui.component.PageItemLayout
import vip.mystery0.xhu.timetable.ui.component.ShowTermDialog
import vip.mystery0.xhu.timetable.ui.component.ShowUserDialog
import vip.mystery0.xhu.timetable.ui.component.ShowYearDialog
import vip.mystery0.xhu.timetable.ui.component.StateScreen
import vip.mystery0.xhu.timetable.ui.component.collectAndHandleState
import vip.mystery0.xhu.timetable.ui.component.itemsIndexed
import vip.mystery0.xhu.timetable.ui.component.xhuHeader
import vip.mystery0.xhu.timetable.ui.navigation.LocalNavController
import vip.mystery0.xhu.timetable.ui.theme.XhuIcons
import vip.mystery0.xhu.timetable.utils.formatWithDecimal
import vip.mystery0.xhu.timetable.viewmodel.AutoScoreSubscribeState
import vip.mystery0.xhu.timetable.viewmodel.ScoreViewModel
import xhutimetable.composeapp.generated.resources.Res
import xhutimetable.composeapp.generated.resources.state_no_data

@Composable
fun QueryScoreScreen() {
    val viewModel = koinViewModel<ScoreViewModel>()

    val navController = LocalNavController.current

    val pager = viewModel.pageState.collectAndHandleState(viewModel::handleLoadState)

    val userSelect by viewModel.userSelect.select.collectAsState()
    val yearSelect by viewModel.yearSelect.select.collectAsState()
    val termSelect by viewModel.termSelect.select.collectAsState()

    val userDialog by viewModel.userSelect.selectDialog.collectAsState()
    val yearDialog by viewModel.yearSelect.selectDialog.collectAsState()
    val termDialog by viewModel.termSelect.selectDialog.collectAsState()

    val autoScoreState by viewModel.autoScoreState.collectAsState()
    var showAutoScoreDialog by remember { mutableStateOf(false) }
    var showStartAutoScoreDialog by remember { mutableStateOf(false) }
    var showStopAutoScoreDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.init()
    }
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = "成绩查询") },
                navigationIcon = {
                    IconButton(onClick = {
                        navController.popBackStack()
                    }) {
                        Icon(
                            painter = XhuIcons.back,
                            contentDescription = null,
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showAutoScoreDialog = true }) {
                        Icon(
                            imageVector = Icons.Rounded.NotificationsActive,
                            contentDescription = "成绩变更提醒",
                            modifier = Modifier.size(24.dp),
                        )
                    }
                }
            )
        },
    ) { paddingValues ->
        var showMoreInfo by remember { mutableStateOf(true) }
        val refreshing by viewModel.refreshing.collectAsState()
        val gpa by viewModel.scoreGpa.collectAsState()
        BuildPaging(
            paddingValues = paddingValues,
            pager = pager,
            refreshing = refreshing,
            listHeader = {
                BuildSelectFilterChipContent(
                    userSelect = userSelect,
                    yearSelect = yearSelect,
                    termSelect = termSelect,
                    showUserDialog = userDialog,
                    showYearDialog = yearDialog,
                    showTermDialog = termDialog,
                    onDataLoad = {
                        viewModel.loadScoreList()
                        viewModel.loadScoreGpa()
                    }
                )
            },
            listContent = {
                if (gpa != null) {
                    item {
                        BuildTermInfo(gpa!!)
                    }
                }
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth()
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(text = "课程成绩列表", fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.weight(1F))
                        Text("显示更多信息", fontSize = 14.sp)
                        Switch(checked = showMoreInfo, onCheckedChange = { showMoreInfo = it })
                    }
                }
                itemsIndexed(pager) { item ->
                    BuildItem(showMoreInfo, item)
                }
            },
            emptyState = {
                val loadingErrorMessage by viewModel.loadingErrorMessage.collectAsState()
                StateScreen(
                    title = loadingErrorMessage ?: "暂无成绩数据",
                    buttonText = "再查一次",
                    imageRes = painterResource(Res.drawable.state_no_data),
                    verticalArrangement = Arrangement.Top,
                    onButtonClick = {
                        viewModel.loadScoreList()
                    }
                )
            }
        )
        if (showAutoScoreDialog) {
            AlertDialog(
                onDismissRequest = { showAutoScoreDialog = false },
                title = { Text("成绩变更提醒") },
                text = {
                    BuildAutoScoreSubscribeContent(
                        state = autoScoreState,
                        onStartClick = {
                            showAutoScoreDialog = false
                            showStartAutoScoreDialog = true
                        },
                        onStopClick = {
                            showAutoScoreDialog = false
                            showStopAutoScoreDialog = true
                        },
                        onRefreshClick = { viewModel.loadAutoScoreStatus() },
                    )
                },
                confirmButton = {},
            )
        }
        if (showStartAutoScoreDialog) {
            AlertDialog(
                onDismissRequest = { showStartAutoScoreDialog = false },
                title = { Text("开启成绩变更提醒") },
                text = {
                    Text("开启后，服务端会在有效期内定时查询成绩。仅当成绩数据发生变化时发送通知，通知内容不包含具体成绩。你可以随时取消。")
                },
                confirmButton = {
                    TextButton(onClick = {
                        showStartAutoScoreDialog = false
                        viewModel.startAutoScoreSubscribe()
                    }) {
                        Text("开启")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showStartAutoScoreDialog = false }) {
                        Text("取消")
                    }
                }
            )
        }
        if (showStopAutoScoreDialog) {
            AlertDialog(
                onDismissRequest = { showStopAutoScoreDialog = false },
                title = { Text("取消成绩变更提醒") },
                text = { Text("取消后，服务端将停止当前成绩查询订阅任务。") },
                confirmButton = {
                    TextButton(onClick = {
                        showStopAutoScoreDialog = false
                        viewModel.stopAutoScoreSubscribe()
                    }) {
                        Text("取消订阅")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showStopAutoScoreDialog = false }) {
                        Text("返回")
                    }
                }
            )
        }
    }
    ShowUserDialog(selectList = userSelect, useCaseState = userDialog, onSelect = {
        viewModel.selectUser(it.studentId)
    })
    ShowYearDialog(selectList = yearSelect, useCaseState = yearDialog, onSelect = {
        viewModel.selectYear(it.value)
    })
    ShowTermDialog(selectList = termSelect, useCaseState = termDialog, onSelect = {
        viewModel.selectTerm(it.value)
    })

    HandleErrorMessage(flow = viewModel.errorMessage)
}

@Composable
private fun BuildAutoScoreSubscribeContent(
    state: AutoScoreSubscribeState,
    onStartClick: () -> Unit,
    onStopClick: () -> Unit,
    onRefreshClick: () -> Unit,
) {
    val active = state.hasActiveTask && state.status == "ACTIVE"
    val statusText = when {
        active -> "已开启"
        state.status == "SUSPENDED" -> "已暂停"
        state.status == "EXPIRED" -> "已到期"
        state.status == "CANCELLED" -> "已取消"
        else -> "未开启"
    }
    Column(
        modifier = Modifier
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text("任务状态：${statusText}", fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.weight(1F))
            if (active) {
                OutlinedButton(
                    enabled = !state.loading,
                    onClick = onStopClick,
                ) {
                    Text("取消")
                }
            } else {
                Button(
                    enabled = !state.loading,
                    onClick = onStartClick,
                ) {
                    Text("开启")
                }
            }
        }
        Text("任务过期时间：${state.expireDate ?: "暂无"}", fontSize = 14.sp)
        Text("下次检查时间：${state.nextCheckTime ?: "暂无"}", fontSize = 14.sp)
        Text("上次执行时间：${state.lastCheckTime ?: "暂无"}", fontSize = 14.sp)
        Text(
            text = "上次执行结果：${state.lastCheckResult ?: "未知"}",
            fontSize = 14.sp,
        )
        if (state.status == "SUSPENDED") {
            Text(
                text = "自动查分已暂停，请检查账号状态后重新开启。",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.error,
            )
        }
        if (state.errorMessage.isNotBlank()) {
            Text(
                text = state.errorMessage,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.error,
            )
        }
        TextButton(
            enabled = !state.loading,
            onClick = onRefreshClick,
        ) {
            Text(if (state.loading) "刷新中..." else "刷新状态")
        }
    }
}

@Composable
private fun BuildTermInfo(gpa: ScoreGpaResponse) {
    val useCaseState = rememberUseCaseState()
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
        ),
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Row {
                BuildTermInfoItem(title = "总成绩", value = gpa.totalScore.formatWithDecimal(2))
                BuildTermInfoItem(title = "平均成绩", value = gpa.averageScore.formatWithDecimal(2))
            }
            Row {
                BuildTermInfoItem(title = "总学分", value = gpa.totalCredit.formatWithDecimal(2))
                BuildTermInfoItem(
                    title = "GPA",
                    value = gpa.gpa.formatWithDecimal(2),
                    showTips = true,
                    onTipsClick = {
                        useCaseState.show()
                    })
            }
        }
    }
    CoreDialog(
        state = useCaseState,
        header = xhuHeader("提示信息"),
        selection = CoreSelection(
            withButtonView = true,
            negativeButton = null,
            onNegativeClick = null,
        ),
        body = {
            Text("GPA为服务端计算，非教务系统真实数据，如果计算错误欢迎进行反馈")
        }
    )
}

@Composable
private fun RowScope.BuildTermInfoItem(
    title: String,
    value: String,
    showTips: Boolean = false,
    onTipsClick: (() -> Unit)? = null,
) {
    Card(
        modifier = Modifier.weight(1F)
            .padding(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
        ),
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier.padding(8.dp),
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = title,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (showTips) {
                        IconButton(
                            modifier = Modifier.size(12.dp),
                            onClick = {
                                onTipsClick?.invoke()
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                }
                Text(
                    text = value,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun BuildItem(
    showMoreInfo: Boolean,
    item: ScoreResponse,
) {
    PageItemLayout(
        cardModifier = Modifier.animateContentSize(),
        header = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = item.courseName, modifier = Modifier.weight(1F))
                Text(
                    text = "${item.score}",
                    color = if (item.score < 60) Color.Red else Color(0XFF4BE683),
                    fontWeight = FontWeight.Bold,
                )
            }
        },
        footer = if (showMoreInfo) {
            {
                Text(
                    text = "${item.courseType} | ${item.scoreType}",
                    fontSize = 14.sp,
                )
                Text(
                    text = "课程学分：${item.credit}",
                    fontSize = 14.sp,
                )
                Text(
                    text = "课程绩点：${item.gpa}",
                    fontSize = 14.sp,
                )
                Text(
                    text = "学分绩点：${item.creditGpa}",
                    fontSize = 14.sp,
                )
                if (item.scoreDescription.isNotBlank()) {
                    Text(
                        text = "成绩说明：${item.scoreDescription}",
                        fontSize = 14.sp,
                    )
                }
            }
        } else null
    )
}
