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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
import vip.mystery0.xhu.timetable.viewmodel.Exam
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
                                BuildItem(item = Exam.EMPTY, placeholder = true)
                            }
                        } else {
                            if (list.isNotEmpty()) {
                                items(list.size) { index ->
                                    BuildItem(item = list[index])
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

@Composable
private fun BuildItem(
    item: Exam,
    placeholder: Boolean = false,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .placeholder(
                visible = placeholder,
                highlight = PlaceholderHighlight.shimmer(),
            ),
        border = BorderStroke(
            item.examStatus.strokeWidth.dp,
            color = item.examStatus.color
        ),
    ) {
        Row(modifier = Modifier.height(IntrinsicSize.Min)) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(72.dp)
                    .background(item.courseColor),
                contentAlignment = Alignment.Center,
            ) {
                val text = when {
                    item.days > 0 -> "${item.days}\n天"
                    item.days < 0 -> "已结束"
                    else -> "今天"
                }
                Text(
                    text = text,
                    color = Color.White,
                    fontSize = 16.sp,
                )
            }
            Column(
                modifier = Modifier
                    .padding(8.dp)
                    .weight(1F),
            ) {
                Text(
                    text = item.courseName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                )
                Text(
                    text = "考试时间：${item.dateString} ${item.time}",
                    color = XhuColor.Common.grayText,
                    fontSize = 14.sp,
                )
                Text(
                    text = "考试地点：${item.location}",
                    color = XhuColor.Common.grayText,
                    fontSize = 14.sp,
                )
                Text(
                    text = "考试类型：${item.type}",
                    color = XhuColor.Common.grayText,
                    fontSize = 14.sp,
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(72.dp)
                    .background(item.examStatus.color),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "座位号\n${item.examNumber}",
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}