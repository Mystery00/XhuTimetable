package vip.mystery0.xhu.timetable.ui.activity

import androidx.activity.compose.BackHandler
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import vip.mystery0.xhu.timetable.R
import vip.mystery0.xhu.timetable.base.BasePageComposeActivity
import vip.mystery0.xhu.timetable.ui.theme.XhuColor
import vip.mystery0.xhu.timetable.ui.theme.XhuIcons
import vip.mystery0.xhu.timetable.viewmodel.Exam
import vip.mystery0.xhu.timetable.viewmodel.ExamViewModel

class ExamActivity : BasePageComposeActivity() {
    private val viewModel: ExamViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun BuildContent() {
        val pager = viewModel.pageState.collectAndHandleState(viewModel::handleLoadState)

        val userSelect by viewModel.userSelect.collectAsState()

        var showUserSelect by remember { mutableStateOf(false) }

        fun onBack() {
            if (showUserSelect) {
                showUserSelect = false
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
                                modifier = Modifier.size(24.dp),
                            )
                        }
                    },
                    actions = {
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
            val refreshing by viewModel.refreshing.collectAsState()
            BuildPaging(
                paddingValues = paddingValues,
                pager = pager,
                refreshing = refreshing,
                itemContent = @Composable { item ->
                    BuildItem(item)
                }
            ) @Composable {
                AnimatedVisibility(
                    modifier = Modifier.align(Alignment.TopEnd),
                    visible = showUserSelect,
                    enter = fadeIn(),
                    exit = fadeOut(),
                ) {
                    Card(
                        modifier = Modifier
                            .padding(4.dp),
                        elevation = CardDefaults.cardElevation(4.dp),
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
        val errorMessage by viewModel.errorMessage.collectAsState()
        if (errorMessage.second.isNotBlank()) {
            errorMessage.second.toast(true)
        }
    }

    override fun onStart() {
        super.onStart()
        pushDynamicShortcuts<ExamActivity>(iconResId = R.drawable.ic_exam)
    }
}

@Composable
private fun BuildItem(
    item: Exam,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
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
                Text(
                    text = item.showText,
                    color = Color.White,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center,
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
                    text = "考试类型：${item.examName}",
                    color = XhuColor.Common.grayText,
                    fontSize = 14.sp,
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(72.dp)
                    .background(item.courseColor),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "座位号\n${item.seatNo}",
                    color = Color.White,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}