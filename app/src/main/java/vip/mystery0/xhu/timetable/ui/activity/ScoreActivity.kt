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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Card
import androidx.compose.material.Checkbox
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.rememberModalBottomSheetState
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
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemsIndexed
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import kotlinx.coroutines.launch
import vip.mystery0.xhu.timetable.R
import vip.mystery0.xhu.timetable.base.BaseSelectComposeActivity
import vip.mystery0.xhu.timetable.model.response.ScoreResponse
import vip.mystery0.xhu.timetable.ui.theme.XhuColor
import vip.mystery0.xhu.timetable.ui.theme.XhuIcons
import vip.mystery0.xhu.timetable.viewmodel.ScoreViewModel

@OptIn(ExperimentalMaterialApi::class)
class ScoreActivity : BaseSelectComposeActivity() {
    private val viewModel: ScoreViewModel by viewModels()

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    override fun BuildContent() {
        val pager = viewModel.pageState.collectAsLazyPagingItems()

        val userSelectStatus = viewModel.userSelect.collectAsState()
        val yearSelectStatus = viewModel.yearSelect.collectAsState()
        val termSelectStatus = viewModel.termSelect.collectAsState()

        val showSelect = rememberModalBottomSheetState(ModalBottomSheetValue.Expanded)
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
                    backgroundColor = MaterialTheme.colors.primary,
                    contentColor = MaterialTheme.colors.onPrimary,
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
            Box {
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
                        }
                    }
                ) {
                    SwipeRefresh(
                        modifier = Modifier
                            .padding(paddingValues)
                            .fillMaxSize(),
                        state = rememberSwipeRefreshState(isRefreshing = pager.loadState.refresh is LoadState.Loading),
                        onRefresh = { pager.refresh() },
                    ) {
                        if (pager.itemCount == 0) {
                            BuildNoDataLayout()
                            return@SwipeRefresh
                        }
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(XhuColor.Common.grayBackground),
                            contentPadding = PaddingValues(4.dp),
                        ) {
//                            stickyHeader {
//                                Text(
//                                    text = "学期总览",
//                                    modifier = Modifier
//                                        .fillMaxWidth()
//                                        .background(XhuColor.Common.whiteBackground)
//                                        .padding(12.dp),
//                                )
//                            }
//                            item {
//                                BuildTermInfo(scoreList)
//                            }
                            stickyHeader {
                                Text(
                                    text = "课程成绩列表",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(XhuColor.Common.whiteBackground)
                                        .padding(12.dp),
                                )
                            }
                            itemsIndexed(pager) { _, item ->
                                item?.let {
                                    BuildItem(showMoreInfo, item)
                                }
                            }
                            when (pager.loadState.append) {
                                is LoadState.Loading -> {
                                    item { BuildPageFooter(text = "数据加载中，请稍后……") }
                                }

                                is LoadState.Error -> {
                                    item { BuildPageFooter(text = "数据加载失败，请重试") }
                                }

                                is LoadState.NotLoading -> {
                                    item { BuildPageFooter(text = "o(´^｀)o 再怎么滑也没有啦~") }
                                }
                            }
                        }
                    }
                }
                AnimatedVisibility(
                    modifier = Modifier.align(Alignment.TopEnd),
                    visible = showOption,
                    enter = fadeIn(),
                    exit = fadeOut(),
                ) {
                    Card(
                        modifier = Modifier
                            .padding(4.dp),
                        elevation = 4.dp,
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
private fun BuildTermInfo(scoreListOrigin: List<ScoreResponse>) {
    //正常考试的列表，也就是不包含重修的列表
    val list = scoreListOrigin.filter { it.scoreType == "正常考试" }
    val successList = list.filter { it.score >= 60 }

    val scoreList = list.map { it.score }

    //总成绩
    val totalScore = scoreList.sum()
    //平均成绩
    val avgScore = scoreList.average()
    //总学分
    val totalCredit = list.sumOf { it.credit }

    //GPA
    val gpa = successList.sumOf { it.score * it.credit } / totalCredit

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        backgroundColor = XhuColor.cardBackground,
    ) {
        Row(modifier = Modifier.padding(8.dp)) {
            Column(
                modifier = Modifier
                    .weight(1F)
                    .padding(start = 16.dp),
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = "总成绩：${totalScore}",
                    fontSize = 13.sp,
                )
                Text(
                    text = "平均成绩：${String.format("%.2f", avgScore)}",
                    fontSize = 13.sp,
                )
                Text(
                    text = "总学分：${totalCredit}",
                    fontSize = 13.sp,
                )
            }
            Column(
                modifier = Modifier
                    .weight(1F)
                    .padding(end = 16.dp),
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = "GPA = ${String.format("%.2f", gpa)}",
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
        backgroundColor = XhuColor.cardBackground,
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
                            text = "课程绩点：${item.creditGpa}",
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
                    color = XhuColor.Common.blackText,
                    fontSize = 16.sp,
                )
            }
        }
    }
}