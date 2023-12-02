package vip.mystery0.xhu.timetable.ui.activity

import androidx.activity.compose.BackHandler
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import vip.mystery0.xhu.timetable.R
import vip.mystery0.xhu.timetable.base.BaseSelectComposeActivity
import vip.mystery0.xhu.timetable.model.response.ScoreGpaResponse
import vip.mystery0.xhu.timetable.model.response.ScoreResponse
import vip.mystery0.xhu.timetable.ui.theme.XhuColor
import vip.mystery0.xhu.timetable.ui.theme.XhuIcons
import vip.mystery0.xhu.timetable.viewmodel.ScoreViewModel

@OptIn(ExperimentalMaterialApi::class)
class ScoreActivity : BaseSelectComposeActivity() {
    private val viewModel: ScoreViewModel by viewModels()

    @OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
    @Composable
    override fun BuildContent() {
        val pager = viewModel.pageState.collectAndHandleState(viewModel::handleLoadState)

        val userSelectStatus = viewModel.userSelect.collectAsState()
        val yearSelectStatus = viewModel.yearSelect.collectAsState()
        val termSelectStatus = viewModel.termSelect.collectAsState()

        val showSelect = rememberModalBottomSheetState(ModalBottomSheetValue.Hidden)
        val scope = rememberCoroutineScope()

        var showOption by remember { mutableStateOf(false) }

        val userDialog = remember { mutableStateOf(false) }
        val yearDialog = remember { mutableStateOf(false) }
        val termDialog = remember { mutableStateOf(false) }

        fun onBack() {
            if (showSelect.isVisible) {
                scope.launch {
                    showSelect.hide()
                }
                return
            }
            if (showOption) {
                showOption = false
                return
            }
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
                    actions = {
                        IconButton(onClick = {
                            scope.launch {
                                showOption = false
                                if (showSelect.isVisible) {
                                    showSelect.hide()
                                } else {
                                    showSelect.show()
                                }
                            }
                        }) {
                            Icon(
                                painter = XhuIcons.Action.more,
                                contentDescription = null,
                                modifier = Modifier.size(24.dp),
                            )
                        }
                        IconButton(onClick = {
                            scope.launch {
                                if (!showSelect.isVisible) {
                                    showOption = !showOption
                                }
                            }
                        }) {
                            Icon(
                                painter = XhuIcons.Action.view,
                                contentDescription = null,
                                modifier = Modifier.size(24.dp),
                            )
                        }
                    }
                )
            },
        ) { paddingValues ->
            var showMoreInfo by remember { mutableStateOf(true) }
            ModalBottomSheetLayout(
                sheetState = showSelect,
                scrimColor = Color.Black.copy(alpha = 0.32f),
                sheetContent = {
                    BuildSelectSheetLayout(
                        bottomSheetState = showSelect,
                        selectUserState = userSelectStatus,
                        selectYearState = yearSelectStatus,
                        selectTermState = termSelectStatus,
                        showUserDialog = userDialog,
                        showYearDialog = yearDialog,
                        showTermDialog = termDialog,
                    ) {
                        viewModel.loadScoreList()
                        viewModel.loadScoreGpa()
                    }
                }
            ) {
                val refreshing by viewModel.refreshing.collectAsState()
                val gpa by viewModel.scoreGpa.collectAsState()
                BuildPaging(
                    paddingValues = paddingValues,
                    pager = pager,
                    refreshing = refreshing,
                    listContent = {
                        if (gpa != null) {
                            stickyHeader {
                                Text(
                                    text = "学期总览",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(XhuColor.Common.whiteBackground)
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
                                    .background(XhuColor.Common.whiteBackground)
                                    .padding(12.dp),
                            )
                        }
                        itemsIndexed(pager) { item ->
                            BuildItem(showMoreInfo, item)
                        }
                    },
                ) {
                    AnimatedVisibility(
                        modifier = Modifier.align(Alignment.TopEnd),
                        visible = showOption,
                        enter = fadeIn(),
                        exit = fadeOut(),
                    ) {
                        Card(
                            modifier = Modifier
                                .padding(4.dp),
                            elevation = CardDefaults.cardElevation(4.dp),
                        ) {
                            Column {
                                Row(modifier = Modifier
                                    .padding(8.dp)
                                    .clickable {
                                        showMoreInfo = !showMoreInfo
                                        showOption = false
                                    }) {
                                    Checkbox(checked = showMoreInfo, onCheckedChange = null)
                                    Text(text = "显示更多信息")
                                }
                            }
                        }
                    }
                }
            }
        }
        ShowUserDialog(selectState = userSelectStatus, show = userDialog, onSelect = {
            viewModel.selectUser(it.studentId)
        })
        ShowYearDialog(selectState = yearSelectStatus, show = yearDialog, onSelect = {
            viewModel.selectYear(it.value)
        })
        ShowTermDialog(selectState = termSelectStatus, show = termDialog, onSelect = {
            viewModel.selectTerm(it.value)
        })

        val errorMessage by viewModel.errorMessage.collectAsState()
        if (errorMessage.second.isNotBlank()) {
            errorMessage.second.toast(true)
        }
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
                        color = XhuColor.Common.blackText,
                    )
                    if (showMoreInfo) {
                        Text(
                            text = item.courseType,
                            color = XhuColor.Common.grayText,
                            fontSize = 14.sp,
                        )
                        Text(
                            text = item.scoreType,
                            color = XhuColor.Common.grayText,
                            fontSize = 14.sp,
                        )
                        Text(
                            text = "课程学分：${item.credit}",
                            color = XhuColor.Common.grayText,
                            fontSize = 14.sp,
                        )
                        Text(
                            text = "课程绩点：${item.gpa}",
                            color = XhuColor.Common.grayText,
                            fontSize = 14.sp,
                        )
                        Text(
                            text = "学分绩点：${item.creditGpa}",
                            color = XhuColor.Common.grayText,
                            fontSize = 14.sp,
                        )
                        if (item.scoreDescription.isNotBlank()) {
                            Text(
                                text = "成绩说明：${item.scoreDescription}",
                                color = XhuColor.Common.grayText,
                                fontSize = 14.sp,
                            )
                        }
                    }
                }
                Text(
                    text = "${item.score}",
                    color = if (item.score < 60) Color.Red else XhuColor.Common.blackText,
                    fontSize = 16.sp,
                )
            }
        }
    }
}