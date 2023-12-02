package vip.mystery0.xhu.timetable.ui.activity

import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cn.jpush.android.api.JPushInterface
import com.vanpra.composematerialdialogs.MaterialDialog
import com.vanpra.composematerialdialogs.MaterialDialogState
import com.vanpra.composematerialdialogs.listItems
import com.vanpra.composematerialdialogs.rememberMaterialDialogState
import com.vanpra.composematerialdialogs.title
import vip.mystery0.xhu.timetable.base.BaseComposeActivity
import vip.mystery0.xhu.timetable.ui.theme.XhuColor
import vip.mystery0.xhu.timetable.ui.theme.XhuIcons
import vip.mystery0.xhu.timetable.viewmodel.JobHistory
import vip.mystery0.xhu.timetable.viewmodel.JobHistoryViewModel

class JobHistoryActivity : BaseComposeActivity() {
    private val viewModel: JobHistoryViewModel by viewModels()

    private val jPushRegistrationId: String? by lazy {
        JPushInterface.getRegistrationID(this)
    }

    @OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
    @Composable
    override fun BuildContent() {
        val historyListState by viewModel.historyListState.collectAsState()

        val addDialogState = rememberMaterialDialogState()

        val lazyListState = rememberLazyListState()

        fun onBack() {
            finish()
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
            floatingActionButton = {
                val isScrollingUp = lazyListState.isScrollingUp()

                AnimatedVisibility(
                    visible = isScrollingUp,
                    enter = slideInVertically(initialOffsetY = { it }),
                    exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
                ) {
                    ExtendedFloatingActionButton(
                        text = {
                            Text(text = "添加任务", color = XhuColor.Common.whiteText)
                        },
                        onClick = {
                            if (jPushRegistrationId.isNullOrBlank()) {
                                toastString("推送注册id为空")
                                return@ExtendedFloatingActionButton
                            }
                            addDialogState.show()
                        },
                        icon = {
                            Icon(
                                imageVector = Icons.Rounded.Add,
                                contentDescription = null,
                                tint = XhuColor.Common.whiteText,
                            )
                        })
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize(),
            ) {
                val pullRefreshState = rememberPullRefreshState(
                    refreshing = historyListState.loading,
                    onRefresh = {
                        viewModel.loadHistoryList()
                    },
                )

                Box(modifier = Modifier.pullRefresh(pullRefreshState)) {
                    val historyList = historyListState.history
                    if (historyListState.loading || historyList.isNotEmpty()) {
                        LazyColumn(
                            state = lazyListState,
                            modifier = Modifier
                                .fillMaxSize()
                                .background(XhuColor.Common.grayBackground),
                            contentPadding = PaddingValues(4.dp),
                        ) {
                            items(historyList.size) { index ->
                                val item = historyList[index]
                                BuildItem(item, jPushRegistrationId)
                            }
                        }
                        PullRefreshIndicator(
                            refreshing = historyListState.loading,
                            state = pullRefreshState,
                            Modifier.align(Alignment.TopCenter),
                        )
                    } else {
                        BuildNoDataLayout()
                    }
                }
            }
        }
        ShowAddDialog(addDialogState)
        val errorMessage by viewModel.errorMessage.collectAsState()
        if (errorMessage.second.isNotBlank()) {
            errorMessage.second.toast(true)
        }
    }

    @Composable
    private fun ShowAddDialog(
        dialogState: MaterialDialogState,
    ) {
        MaterialDialog(dialogState = dialogState) {
            title("请选择需要添加的任务类型")
            listItems(list = listOf("自动查询成绩")) { index, _ ->
                when (index) {
                    0 -> {
                        jPushRegistrationId?.let {
                            viewModel.addAutoCheckScoreJob(it)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BuildItem(item: JobHistory, jPushRegistrationId: String?) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = XhuColor.cardBackground,
            ),
        ) {
            Column(modifier = Modifier.padding(8.dp)) {
                Text(
                    text = "任务类型：${item.jobTypeTitle}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = XhuColor.Common.blackText,
                )
                Text(
                    text = "通知设备：${if (item.registrationId == jPushRegistrationId) "本机" else "非本机"}",
                    fontSize = 14.sp,
                    color = XhuColor.Common.blackText,
                )
                Text(
                    text = "预期执行时间：${item.prepareExecuteTime}",
                    color = XhuColor.Common.grayText,
                    fontSize = 14.sp,
                )
                Text(
                    text = "任务执行时间：${item.executeTime}",
                    color = XhuColor.Common.grayText,
                    fontSize = 14.sp,
                )
                Text(
                    text = "任务执行信息：${item.message}",
                    color = XhuColor.Common.grayText,
                    fontSize = 14.sp,
                )
            }
        }
        if (item.showStatus) {
            when {
                item.success -> {
                    Image(
                        painter = XhuIcons.cornerSuccess,
                        contentDescription = null,
                        modifier = Modifier
                            .size(48.dp)
                            .align(Alignment.TopEnd),
                    )
                }

                item.failed -> {
                    Image(
                        painter = XhuIcons.cornerFailed,
                        contentDescription = null,
                        modifier = Modifier
                            .size(48.dp)
                            .align(Alignment.TopEnd),
                    )
                }
            }
        }
    }
}