package vip.mystery0.xhu.timetable.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.UnfoldLess
import androidx.compose.material.icons.rounded.UnfoldMore
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel
import vip.mystery0.xhu.timetable.base.HandleErrorMessage
import vip.mystery0.xhu.timetable.model.response.ScoreGpaResponse
import vip.mystery0.xhu.timetable.model.response.ScoreResponse
import vip.mystery0.xhu.timetable.ui.component.BuildPaging
import vip.mystery0.xhu.timetable.ui.component.BuildSelectFilterChipContent
import vip.mystery0.xhu.timetable.ui.component.ShowTermDialog
import vip.mystery0.xhu.timetable.ui.component.ShowUserDialog
import vip.mystery0.xhu.timetable.ui.component.ShowYearDialog
import vip.mystery0.xhu.timetable.ui.component.StateScreen
import vip.mystery0.xhu.timetable.ui.component.collectAndHandleState
import vip.mystery0.xhu.timetable.ui.component.dialog.XhuConfirmDialog
import vip.mystery0.xhu.timetable.ui.component.dialog.XhuDialog
import vip.mystery0.xhu.timetable.ui.component.dialog.XhuInfoDialog
import vip.mystery0.xhu.timetable.ui.component.dialog.rememberXhuDialogState
import vip.mystery0.xhu.timetable.ui.component.itemsIndexed
import vip.mystery0.xhu.timetable.ui.navigation.LocalNavController
import vip.mystery0.xhu.timetable.ui.theme.XhuColor
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

    val userDialogState = rememberXhuDialogState()
    val yearDialogState = rememberXhuDialogState()
    val termDialogState = rememberXhuDialogState()

    val autoScoreState by viewModel.autoScoreState.collectAsState()
    val autoScoreDialogState = rememberXhuDialogState()
    val startAutoScoreDialogState = rememberXhuDialogState()
    val stopAutoScoreDialogState = rememberXhuDialogState()

    var expandAll by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        viewModel.init()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "成绩查询",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            painter = XhuIcons.back,
                            contentDescription = "返回",
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { autoScoreDialogState.show() }) {
                        Icon(
                            imageVector = Icons.Rounded.NotificationsActive,
                            contentDescription = "成绩变更提醒",
                            modifier = Modifier.size(22.dp),
                        )
                    }
                }
            )
        },
    ) { paddingValues ->
        val refreshing by viewModel.refreshing.collectAsState()
        val gpa by viewModel.scoreGpa.collectAsState()
        BuildPaging(
            paddingValues = paddingValues,
            pager = pager,
            refreshing = refreshing,
            listHeader = {
                vip.mystery0.xhu.timetable.ui.component.BuildSelectFilterBar(
                    userSelect = userSelect,
                    yearSelect = yearSelect,
                    termSelect = termSelect,
                    onUserClick = { userDialogState.show() },
                    onYearClick = { yearDialogState.show() },
                    onTermClick = { termDialogState.show() },
                    onRefresh = {
                        viewModel.loadScoreList()
                        viewModel.loadScoreGpa()
                    }
                )
            },
            listContent = {
                gpa?.let { gpaData ->
                    item(key = "score_gpa_summary") {
                        BuildTermInfo(gpaData)
                    }
                }
                item(key = "score_list_header") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 20.dp, end = 12.dp, top = 8.dp, bottom = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "课程成绩",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        TextButton(onClick = { expandAll = !expandAll }) {
                            Icon(
                                imageVector = if (expandAll) Icons.Rounded.UnfoldLess else Icons.Rounded.UnfoldMore,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                            )
                            Spacer(modifier = Modifier.size(4.dp))
                            Text(if (expandAll) "收起详情" else "展开详情")
                        }
                    }
                }
                itemsIndexed(pager) { item ->
                    BuildScoreCard(defaultExpanded = expandAll, item = item)
                }
            },
            emptyState = {
                val loadingErrorMessage by viewModel.loadingErrorMessage.collectAsState()
                StateScreen(
                    title = loadingErrorMessage ?: "暂无成绩数据",
                    buttonText = "再查一次",
                    imageRes = painterResource(Res.drawable.state_no_data),
                    onButtonClick = {
                        viewModel.loadScoreList()
                        viewModel.loadScoreGpa()
                    }
                )
            }
        )
    }

    // 成绩变更提醒管理弹窗
    XhuDialog(
        state = autoScoreDialogState,
        title = "成绩变更提醒",
        icon = { Icon(Icons.Rounded.NotificationsActive, contentDescription = null) },
        dismissButtonText = "关闭",
        extraButtonText = if (autoScoreState.loading) "刷新中…" else "刷新状态",
        onExtraButtonClick = {
            if (!autoScoreState.loading) viewModel.loadAutoScoreStatus()
        },
    ) {
        BuildAutoScoreSubscribeContent(
            state = autoScoreState,
            onStartClick = {
                autoScoreDialogState.hide()
                startAutoScoreDialogState.show()
            },
            onStopClick = {
                autoScoreDialogState.hide()
                stopAutoScoreDialogState.show()
            },
        )
    }

    XhuConfirmDialog(
        state = startAutoScoreDialogState,
        title = "开启成绩变更提醒",
        message = "开启后，服务端会在有效期内定时查询成绩。仅当成绩数据发生变化时发送通知，通知内容不包含具体成绩。你可以随时取消。",
        confirmText = "确认开启",
        onConfirm = { viewModel.startAutoScoreSubscribe() },
    )

    XhuConfirmDialog(
        state = stopAutoScoreDialogState,
        title = "取消成绩变更提醒",
        message = "取消后，服务端将停止当前成绩查询订阅任务。",
        confirmText = "取消订阅",
        dismissText = "返回",
        isDestructive = true,
        onConfirm = { viewModel.stopAutoScoreSubscribe() },
    )

    ShowUserDialog(selectList = userSelect, state = userDialogState, onSelect = {
        viewModel.selectUser(it.studentId)
    })
    ShowYearDialog(selectList = yearSelect, state = yearDialogState, onSelect = {
        viewModel.selectYear(it.value)
    })
    ShowTermDialog(selectList = termSelect, state = termDialogState, onSelect = {
        viewModel.selectTerm(it.value)
    })

    HandleErrorMessage(flow = viewModel.errorMessage)
}

@Composable
private fun BuildAutoScoreSubscribeContent(
    state: AutoScoreSubscribeState,
    onStartClick: () -> Unit,
    onStopClick: () -> Unit,
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
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.surfaceContainerLowest,
            border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "当前订阅状态",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = statusText,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                    )
                }
                if (active) {
                    OutlinedButton(
                        enabled = !state.loading,
                        onClick = onStopClick,
                    ) {
                        Text("取消订阅")
                    }
                } else {
                    Button(
                        enabled = !state.loading,
                        onClick = onStartClick,
                    ) {
                        Text("开启提醒")
                    }
                }
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            SubscribeMetaLine("任务过期时间", state.expireDate ?: "暂无")
            SubscribeMetaLine("下次检查时间", state.nextCheckTime ?: "暂无")
            SubscribeMetaLine("上次执行时间", state.lastCheckTime ?: "暂无")
            SubscribeMetaLine("上次执行结果", state.lastCheckResult ?: "未知")
        }

        if (state.status == "SUSPENDED") {
            Text(
                text = "自动查分已暂停，请检查账号状态后重新开启。",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
            )
        }
        if (state.errorMessage.isNotBlank()) {
            Text(
                text = state.errorMessage,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
            )
        }
    }
}

@Composable
private fun SubscribeMetaLine(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun BuildTermInfo(gpa: ScoreGpaResponse) {
    val gpaInfoState = rememberXhuDialogState()

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "学期平均绩点 (GPA)",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                    )
                    Text(
                        text = gpa.gpa.formatWithDecimal(2),
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
                IconButton(
                    onClick = { gpaInfoState.show() },
                    modifier = Modifier.size(40.dp),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Info,
                        contentDescription = "GPA 计算说明",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
            }

            HorizontalDivider(
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.14f)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                TermMetricColumn(
                    label = "平均成绩",
                    value = gpa.averageScore.formatWithDecimal(2),
                    modifier = Modifier.weight(1f),
                )
                TermMetricColumn(
                    label = "总学分",
                    value = gpa.totalCredit.formatWithDecimal(2),
                    modifier = Modifier.weight(1f),
                )
                TermMetricColumn(
                    label = "总成绩",
                    value = gpa.totalScore.formatWithDecimal(2),
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }

    XhuInfoDialog(
        state = gpaInfoState,
        title = "GPA 计算说明",
        icon = { Icon(Icons.Outlined.Info, contentDescription = null) },
        message = "当前展示的 GPA 为服务端根据课程学分与绩点自动计算所得，并非教务系统官方直接返回的数值。如发现计算结果与教务系统存在差异，欢迎通过意见反馈告知我们。",
    )
}

@Composable
private fun TermMetricColumn(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f),
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun BuildScoreCard(
    defaultExpanded: Boolean,
    item: ScoreResponse,
) {
    var expanded by remember(defaultExpanded) { mutableStateOf(defaultExpanded) }
    val scoreColor = XhuColor.Score.colorForScore(item.score)
    val scoreBg = XhuColor.Score.containerForScore(item.score)

    Card(
        onClick = { expanded = !expanded },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 5.dp)
            .animateContentSize(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = XhuColor.cardBackground),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.courseName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = "${item.courseType} · ${item.scoreType}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = scoreBg,
                ) {
                    Text(
                        text = "${item.score}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = scoreColor,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    )
                }
            }

            AnimatedVisibility(visible = expanded) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        ScoreTagPill(label = "学分 ${item.credit}")
                        ScoreTagPill(label = "绩点 ${item.gpa}")
                        ScoreTagPill(label = "学分绩点 ${item.creditGpa}")
                    }
                    if (item.scoreDescription.isNotBlank()) {
                        Text(
                            text = "成绩说明：${item.scoreDescription}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

@Composable
internal fun ScoreTagPill(label: String) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.8f),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
        )
    }
}
