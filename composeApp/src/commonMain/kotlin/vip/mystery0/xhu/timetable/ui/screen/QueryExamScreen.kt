package vip.mystery0.xhu.timetable.ui.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Assignment
import androidx.compose.material.icons.rounded.AccessTime
import androidx.compose.material.icons.rounded.EventSeat
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.TipsAndUpdates
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import multiplatform.network.cmptoast.showToast
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel
import vip.mystery0.xhu.timetable.base.HandleErrorMessage
import vip.mystery0.xhu.timetable.ui.component.BuildPaging
import vip.mystery0.xhu.timetable.ui.component.BuildSelectFilterChipContent
import vip.mystery0.xhu.timetable.ui.component.ShowTermDialog
import vip.mystery0.xhu.timetable.ui.component.ShowUserDialog
import vip.mystery0.xhu.timetable.ui.component.ShowYearDialog
import vip.mystery0.xhu.timetable.ui.component.StateScreen
import vip.mystery0.xhu.timetable.ui.component.collectAndHandleState
import vip.mystery0.xhu.timetable.ui.component.dialog.XhuInfoDialog
import vip.mystery0.xhu.timetable.ui.component.dialog.rememberXhuDialogState
import vip.mystery0.xhu.timetable.ui.component.itemsIndexed
import vip.mystery0.xhu.timetable.ui.navigation.LocalNavController
import vip.mystery0.xhu.timetable.ui.theme.CourseColorHelper
import vip.mystery0.xhu.timetable.ui.theme.XhuColor
import vip.mystery0.xhu.timetable.ui.theme.XhuIcons
import vip.mystery0.xhu.timetable.utils.copyToClipboard
import vip.mystery0.xhu.timetable.viewmodel.Exam
import vip.mystery0.xhu.timetable.viewmodel.ExamStatus
import vip.mystery0.xhu.timetable.viewmodel.ExamViewModel
import xhutimetable.composeapp.generated.resources.Res
import xhutimetable.composeapp.generated.resources.state_no_exam

@Composable
fun QueryExamScreen() {
    val viewModel = koinViewModel<ExamViewModel>()
    val navController = LocalNavController.current
    val pager = viewModel.pageState.collectAndHandleState(viewModel::handleLoadState)

    val userSelect by viewModel.userSelect.select.collectAsState()
    val yearSelect by viewModel.yearSelect.select.collectAsState()
    val termSelect by viewModel.termSelect.select.collectAsState()

    val userDialogState = rememberXhuDialogState()
    val yearDialogState = rememberXhuDialogState()
    val termDialogState = rememberXhuDialogState()

    val tipsDialogState = rememberXhuDialogState()

    LaunchedEffect(Unit) {
        viewModel.init()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "考试查询",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            painter = XhuIcons.back,
                            contentDescription = "返回",
                            modifier = Modifier.size(24.dp),
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { tipsDialogState.show() }) {
                        Icon(
                            imageVector = Icons.Rounded.TipsAndUpdates,
                            contentDescription = "补考查询提示",
                            modifier = Modifier.size(22.dp),
                        )
                    }
                }
            )
        },
    ) { paddingValues ->
        val refreshing by viewModel.refreshing.collectAsState()
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
                    onRefresh = { viewModel.loadExamList() }
                )
            },
            listContent = {
                itemsIndexed(pager) { item ->
                    BuildExamCard(item)
                }
            },
            emptyState = {
                val loadingErrorMessage by viewModel.loadingErrorMessage.collectAsState()
                StateScreen(
                    title = loadingErrorMessage ?: "当前学期暂无考试安排",
                    subtitle = "如需查询补考信息，可点击顶部学年/学期切换至上一学期",
                    buttonText = "重新查询",
                    imageRes = painterResource(Res.drawable.state_no_exam),
                    onButtonClick = {
                        viewModel.loadExamList()
                    }
                )
            }
        )
    }

    ShowUserDialog(selectList = userSelect, state = userDialogState, onSelect = {
        viewModel.selectUser(it.studentId)
    })
    ShowYearDialog(selectList = yearSelect, state = yearDialogState, onSelect = {
        viewModel.selectYear(it.value)
    })
    ShowTermDialog(selectList = termSelect, state = termDialogState, onSelect = {
        viewModel.selectTerm(it.value)
    })

    XhuInfoDialog(
        state = tipsDialogState,
        title = "补考查询提示",
        icon = { Icon(Icons.Rounded.TipsAndUpdates, contentDescription = null) },
        message = "教务系统的补考安排通常归属在上一学期中。如需查询补考信息，请在顶部筛选栏将学年与学期切换至上一个学期即可。",
    )

    HandleErrorMessage(flow = viewModel.errorMessage)
}

@Composable
private fun BuildExamCard(item: Exam) {
    val isFinished = item.examStatus == ExamStatus.AFTER
    val isToday = item.examStatus == ExamStatus.IN

    val accent = when {
        isFinished -> MaterialTheme.colorScheme.onSurfaceVariant
        isToday -> XhuColor.Status.inContent()
        else -> CourseColorHelper.accentText(item.courseColor)
    }
    val badgeBg = when {
        isFinished -> MaterialTheme.colorScheme.surfaceContainerHighest
        isToday -> XhuColor.Status.inContainer()
        else -> accent.copy(alpha = 0.13f)
    }

    Card(
        onClick = {
            val summary = "${item.courseName} | ${item.dateString} ${item.time} | ${item.location} 座位号 ${item.seatNo}"
            copyToClipboard(summary)
            showToast("已复制考试信息")
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .alpha(if (isFinished) 0.68f else 1f),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = XhuColor.cardBackground,
        ),
        border = if (isToday) {
            BorderStroke(1.2.dp, accent.copy(alpha = 0.7f))
        } else {
            BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
        },
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
                .padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // 左侧紧凑倒计时徽章
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = badgeBg,
                modifier = Modifier
                    .width(62.dp)
                    .fillMaxHeight(),
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 10.dp),
                ) {
                    Text(
                        text = item.showText,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = accent,
                        textAlign = TextAlign.Center,
                    )
                }
            }

            // 中间考试信息主体
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = item.courseName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                    if (item.seatNo.isNotBlank()) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isFinished) {
                                MaterialTheme.colorScheme.surfaceContainerHighest
                            } else {
                                MaterialTheme.colorScheme.primaryContainer
                            },
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(3.dp),
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.EventSeat,
                                    contentDescription = null,
                                    tint = if (isFinished) {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    } else {
                                        MaterialTheme.colorScheme.onPrimaryContainer
                                    },
                                    modifier = Modifier.size(13.dp),
                                )
                                Text(
                                    text = "${item.seatNo}座",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isFinished) {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    } else {
                                        MaterialTheme.colorScheme.onPrimaryContainer
                                    },
                                )
                            }
                        }
                    }
                }

                ExamMetaRow(
                    icon = Icons.Rounded.AccessTime,
                    text = "${item.dateString}  ${item.time}",
                )
                ExamMetaRow(
                    icon = Icons.Rounded.LocationOn,
                    text = item.location,
                )
                if (item.examName.isNotBlank()) {
                    ExamMetaRow(
                        icon = Icons.AutoMirrored.Rounded.Assignment,
                        text = item.examName,
                    )
                }
            }
        }
    }
}

@Composable
private fun ExamMetaRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(14.dp),
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
