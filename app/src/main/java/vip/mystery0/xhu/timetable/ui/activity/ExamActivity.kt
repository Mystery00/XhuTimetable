package vip.mystery0.xhu.timetable.ui.activity

import androidx.activity.viewModels
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
        val selectUser by viewModel.selectUser.collectAsState()
        val examListState by viewModel.examListState.collectAsState()
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(text = title.toString()) },
                    backgroundColor = MaterialTheme.colors.primary,
                    contentColor = MaterialTheme.colors.onPrimary,
                    navigationIcon = {
                        IconButton(onClick = {
                            finish()
                        }) {
                            Icon(XhuIcons.back, "")
                        }
                    },
                    actions = {
                        Text(
                            text = "切换用户",
                            modifier = Modifier.clickable(
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() },
                            ) {

                            })
                    }
                )
            },
        ) { paddingValues ->
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
        }
        if (examListState.errorMessage.isNotBlank()) {
            examListState.errorMessage.toast(true)
        }
    }
}