package vip.mystery0.xhu.timetable.ui.screen

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
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
import vip.mystery0.xhu.timetable.ui.component.itemsIndexed
import vip.mystery0.xhu.timetable.ui.navigation.LocalNavController
import vip.mystery0.xhu.timetable.ui.theme.XhuColor
import vip.mystery0.xhu.timetable.ui.theme.XhuIcons
import vip.mystery0.xhu.timetable.utils.formatWithDecimal
import vip.mystery0.xhu.timetable.viewmodel.ScoreViewModel
import xhutimetable.composeapp.generated.resources.Res
import xhutimetable.composeapp.generated.resources.state_no_data

@Composable
fun QueryScoreScreen() {
    val viewModel = koinViewModel<ScoreViewModel>()

    val navController = LocalNavController.current!!

    val pager = viewModel.pageState.collectAndHandleState(viewModel::handleLoadState)

    val userSelect by viewModel.userSelect.select.collectAsState()
    val yearSelect by viewModel.yearSelect.select.collectAsState()
    val termSelect by viewModel.termSelect.select.collectAsState()

    val userDialog by viewModel.userSelect.selectDialog.collectAsState()
    val yearDialog by viewModel.yearSelect.selectDialog.collectAsState()
    val termDialog by viewModel.termSelect.selectDialog.collectAsState()

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
                stickyHeader {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceContainer)
                            .padding(12.dp)
                            .clickable {
                                showMoreInfo = !showMoreInfo
                            },
                    ) {
                        Text(text = "显示更多信息")
                        Spacer(modifier = Modifier.weight(1F))
                        Switch(checked = showMoreInfo, onCheckedChange = null)
                    }
                }
                if (gpa != null) {
                    stickyHeader {
                        Text(
                            text = "学期总览",
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surfaceContainer)
                                .padding(12.dp),
                        )
                    }
                    item {
                        BuildTermInfo(gpa!!)
                    }
                }
                stickyHeader {
                    Text(
                        text = "课程成绩列表",
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceContainer)
                            .padding(12.dp),
                    )
                }
                itemsIndexed(pager) { item ->
                    BuildItem(showMoreInfo, item)
                }
            },
            emptyState = {
                StateScreen(
                    title = "暂无成绩数据",
                    buttonText = "再查一次",
                    imageRes = painterResource(Res.drawable.state_no_data),
                    verticalArrangement = Arrangement.Top,
                    onButtonClick = {
                        viewModel.loadScoreList()
                    }
                )
            }
        )
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
private fun BuildTermInfo(gpa: ScoreGpaResponse) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = XhuColor.cardBackground,
        ),
    ) {
        Row(modifier = Modifier.padding(8.dp)) {
            Column(
                modifier = Modifier
                    .weight(1F),
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = "总成绩：${gpa.totalScore.formatWithDecimal(2)}",
                    fontSize = 13.sp,
                )
                Text(
                    text = "平均成绩：${gpa.averageScore.formatWithDecimal(2)}",
                    fontSize = 13.sp,
                )
                Text(
                    text = "总学分：${gpa.totalCredit.formatWithDecimal(2)}",
                    fontSize = 13.sp,
                )
            }
            Column(
                modifier = Modifier
                    .weight(1F),
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = "GPA = ${gpa.gpa.formatWithDecimal(2)}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
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
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .animateContentSize(),
        colors = CardDefaults.cardColors(
            containerColor = XhuColor.cardBackground,
        ),
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1F)) {
                    Text(
                        text = item.courseName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    if (showMoreInfo) {
                        Text(
                            text = item.courseType,
                            color = MaterialTheme.colorScheme.outline,
                            fontSize = 14.sp,
                        )
                        Text(
                            text = item.scoreType,
                            color = MaterialTheme.colorScheme.outline,
                            fontSize = 14.sp,
                        )
                        Text(
                            text = "课程学分：${item.credit}",
                            color = MaterialTheme.colorScheme.outline,
                            fontSize = 14.sp,
                        )
                        Text(
                            text = "课程绩点：${item.gpa}",
                            color = MaterialTheme.colorScheme.outline,
                            fontSize = 14.sp,
                        )
                        Text(
                            text = "学分绩点：${item.creditGpa}",
                            color = MaterialTheme.colorScheme.outline,
                            fontSize = 14.sp,
                        )
                        if (item.scoreDescription.isNotBlank()) {
                            Text(
                                text = "成绩说明：${item.scoreDescription}",
                                color = MaterialTheme.colorScheme.outline,
                                fontSize = 14.sp,
                            )
                        }
                    }
                }
                Text(
                    text = "${item.score}",
                    color = if (item.score < 60) Color.Red else MaterialTheme.colorScheme.onSurface,
                    fontSize = 16.sp,
                )
            }
        }
    }
}