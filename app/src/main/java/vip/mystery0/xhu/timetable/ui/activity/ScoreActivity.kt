package vip.mystery0.xhu.timetable.ui.activity

import androidx.activity.compose.BackHandler
import androidx.activity.viewModels
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
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
import vip.mystery0.xhu.timetable.R
import vip.mystery0.xhu.timetable.base.BaseSelectComposeActivity
import vip.mystery0.xhu.timetable.model.response.ScoreGpaResponse
import vip.mystery0.xhu.timetable.model.response.ScoreResponse
import vip.mystery0.xhu.timetable.ui.component.rememberXhuDialogState
import vip.mystery0.xhu.timetable.ui.theme.ExtendedTheme
import vip.mystery0.xhu.timetable.ui.theme.XhuColor
import vip.mystery0.xhu.timetable.ui.theme.XhuIcons
import vip.mystery0.xhu.timetable.viewmodel.ScoreViewModel

class ScoreActivity : BaseSelectComposeActivity() {
    private val viewModel: ScoreViewModel by viewModels()

    @OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
    @Composable
    override fun BuildContent() {
        val pager = viewModel.pageState.collectAndHandleState(viewModel::handleLoadState)

        val userSelect by viewModel.userSelect.collectAsState()
        val yearSelect by viewModel.yearSelect.collectAsState()
        val termSelect by viewModel.termSelect.collectAsState()

        val userDialog = rememberXhuDialogState()
        val yearDialog = rememberXhuDialogState()
        val termDialog = rememberXhuDialogState()

        fun onBack() {
            finish()
        }

        BackHandler {
            onBack()
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(text = title.toString()) },
                    navigationIcon = {
                        IconButton(onClick = {
                            onBack()
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
                                .background(ExtendedTheme.colorScheme.surfaceContainer)
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
                                    .background(ExtendedTheme.colorScheme.surfaceContainer)
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
                                .background(ExtendedTheme.colorScheme.surfaceContainer)
                                .padding(12.dp),
                        )
                    }
                    itemsIndexed(pager) { item ->
                        BuildItem(showMoreInfo, item)
                    }
                },
            )
        }
        ShowUserDialog(selectList = userSelect, show = userDialog, onSelect = {
            viewModel.selectUser(it.studentId)
        })
        ShowYearDialog(selectList = yearSelect, show = yearDialog, onSelect = {
            viewModel.selectYear(it.value)
        })
        ShowTermDialog(selectList = termSelect, show = termDialog, onSelect = {
            viewModel.selectTerm(it.value)
        })

        HandleErrorMessage(flow = viewModel.errorMessage)
    }

    override fun onStart() {
        super.onStart()
        pushDynamicShortcuts<ScoreActivity>(iconResId = R.drawable.ic_score)
    }
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
                    text = "总成绩：${String.format("%.2f", gpa.totalScore)}",
                    fontSize = 13.sp,
                )
                Text(
                    text = "平均成绩：${String.format("%.2f", gpa.averageScore)}",
                    fontSize = 13.sp,
                )
                Text(
                    text = "总学分：${String.format("%.2f", gpa.totalCredit)}",
                    fontSize = 13.sp,
                )
            }
            Column(
                modifier = Modifier
                    .weight(1F),
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = "GPA = ${String.format("%.2f", gpa.gpa)}",
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