package vip.mystery0.xhu.timetable.ui.activity

import androidx.activity.compose.BackHandler
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.placeholder.PlaceholderHighlight
import com.google.accompanist.placeholder.material.placeholder
import com.google.accompanist.placeholder.material.shimmer
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import vip.mystery0.xhu.timetable.base.BaseComposeActivity
import vip.mystery0.xhu.timetable.ui.theme.XhuColor
import vip.mystery0.xhu.timetable.ui.theme.XhuIcons
import vip.mystery0.xhu.timetable.viewmodel.ExamViewModel

class ExamActivity : BaseComposeActivity() {
    private val viewModel: ExamViewModel by viewModels()

    @Composable
    override fun BuildContent() {
        val userSelect by viewModel.userSelect.collectAsState()
        val examListState by viewModel.examListState.collectAsState()
        var showUserSelect by remember { mutableStateOf(false) }

        fun onBack() {
            if (showUserSelect) {
                showUserSelect = false
                return
            }
            finish()
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
                                modifier = Modifier.size(24.dp),
                            )
                        }
                    },
                    actions = {
                        if (examListState.examHtml.isNotBlank()) {
                            Text(
                                text = "显示原始网页",
                                modifier = Modifier.clickable(
                                    indication = null,
                                    interactionSource = remember { MutableInteractionSource() },
                                ) {
                                    examListState.examHtml.toast()
                                })
                        }
                        IconButton(onClick = {
                            showUserSelect = !showUserSelect
                        }) {
                            Icon(
                                painter = XhuIcons.Action.more,
                                contentDescription = null,
                            )
                        }
                    }
                )
            },
        ) { paddingValues ->
            Box {
                SwipeRefresh(
                    modifier = Modifier
                        .padding(paddingValues)
                        .fillMaxSize(),
                    state = rememberSwipeRefreshState(examListState.loading),
                    onRefresh = { viewModel.loadExamList() },
                ) {
                    val list = examListState.examList
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(XhuColor.Common.grayBackground),
                        contentPadding = PaddingValues(4.dp),
                    ) {
                        if (examListState.loading) {
                            items(3) {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                        .placeholder(
                                            visible = examListState.loading,
                                            highlight = PlaceholderHighlight.shimmer(),
                                        )
                                ) {
                                    Column(
                                        modifier = Modifier.padding(4.dp)
                                    ) {
                                        Text(
                                            text = "课程名称",
                                            fontSize = 16.sp,
                                        )
                                        Text(
                                            text = "考试日期",
                                            fontSize = 14.sp,
                                        )
                                        Text(
                                            text = "考试时间",
                                            fontSize = 14.sp,
                                        )
                                        Text(
                                            text = "考试地点",
                                            fontSize = 14.sp,
                                        )
                                        Text(
                                            text = "座位号",
                                            fontSize = 14.sp,
                                        )
                                        Text(
                                            text = "考试类型",
                                            fontSize = 14.sp,
                                        )
                                    }
                                }
                            }
                        } else {
                            if (list.isNotEmpty()) {
                                items(list.size) { index ->
                                    val item = list[index]
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 8.dp, vertical = 4.dp),
                                        border = BorderStroke(1.dp, color = item.examStatus.color),
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(8.dp)
                                        ) {
                                            Text(
                                                text = item.courseName,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 16.sp,
                                            )
                                            Text(
                                                text = "考试日期：${item.dateString}",
                                                color = XhuColor.Common.grayText,
                                                fontSize = 14.sp,
                                            )
                                            Text(
                                                text = "考试时间：${item.time}",
                                                color = XhuColor.Common.grayText,
                                                fontSize = 14.sp,
                                            )
                                            Text(
                                                text = "考试地点：${item.location}",
                                                color = XhuColor.Common.grayText,
                                                fontSize = 14.sp,
                                            )
                                            Text(
                                                text = "座位号：${item.examNumber}",
                                                color = XhuColor.Common.grayText,
                                                fontSize = 14.sp,
                                            )
                                            Text(
                                                text = "考试类型：${item.type}",
                                                color = XhuColor.Common.grayText,
                                                fontSize = 14.sp,
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                    if (!examListState.loading && list.isEmpty()) {
                        BuildNoDataLayout()
                    }
                }
                AnimatedVisibility(
                    modifier = Modifier.align(Alignment.TopEnd),
                    visible = showUserSelect,
                    enter = fadeIn(),
                    exit = fadeOut(),
                ) {
                    Card(
                        modifier = Modifier
                            .padding(4.dp),
                        elevation = 4.dp,
                    ) {
                        LazyColumn {
                            items(userSelect.size) { index ->
                                val user = userSelect[index]
                                Row(modifier = Modifier
                                    .padding(8.dp)
                                    .clickable {
                                        viewModel.selectUser(user.studentId)
                                        showUserSelect = false
                                    }) {
                                    RadioButton(selected = user.selected, onClick = null)
                                    Text(text = "${user.userName}(${user.studentId})")
                                }
                            }
                        }
                    }
                }
            }
        }
        if (examListState.errorMessage.isNotBlank()) {
            examListState.errorMessage.toast(true)
        }
        BackHandler(
            enabled = showUserSelect,
            onBack = {
                onBack()
            }
        )
    }
}