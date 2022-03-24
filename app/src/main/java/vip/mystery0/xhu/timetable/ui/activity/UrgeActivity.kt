package vip.mystery0.xhu.timetable.ui.activity

import androidx.activity.viewModels
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.zyao89.view.zloading.Z_TYPE
import vip.mystery0.xhu.timetable.appName
import vip.mystery0.xhu.timetable.base.BaseComposeActivity
import vip.mystery0.xhu.timetable.config.DataHolder
import vip.mystery0.xhu.timetable.config.chinaZone
import vip.mystery0.xhu.timetable.model.response.UrgeItem
import vip.mystery0.xhu.timetable.ui.theme.XhuColor
import vip.mystery0.xhu.timetable.ui.theme.XhuIcons
import vip.mystery0.xhu.timetable.utils.chinaDateTimeFormatter
import vip.mystery0.xhu.timetable.viewmodel.UrgeViewModel
import java.time.Instant
import java.time.LocalDateTime

class UrgeActivity : BaseComposeActivity() {
    private val viewModel: UrgeViewModel by viewModels()

    @Composable
    override fun BuildContent() {
        val urgeListState by viewModel.urgeListState.collectAsState()
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
                            Icon(
                                painter = XhuIcons.back,
                                contentDescription = null,
                            )
                        }
                    },
                )
            },
        ) { paddingValues ->
            SwipeRefresh(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize(),
                state = rememberSwipeRefreshState(urgeListState.loading),
                onRefresh = { viewModel.loadUrgeList() },
            ) {
                Column {
                    BuildTopDesc(remainCount = urgeListState.remainCount)
                    val list = urgeListState.urgeList
                    var expandItemIndex by remember { mutableStateOf(-1) }
                    if (urgeListState.loading || list.isNotEmpty()) {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(XhuColor.Common.grayBackground),
                            contentPadding = PaddingValues(4.dp),
                        ) {
                            if (list.isNotEmpty()) {
                                items(list.size) { index ->
                                    BuildItem(
                                        urgeItem = list[index],
                                        expandItemIndex == index,
                                        onClick = {
                                            expandItemIndex =
                                                if (expandItemIndex != index) index else -1
                                        }) {
                                        viewModel.urge(it.urgeId)
                                    }
                                }
                            }
                        }
                    } else {
                        BuildNoDataLayout()
                    }
                }
            }
        }
        if (urgeListState.errorMessage.isNotBlank()) {
            urgeListState.errorMessage.toast(true)
        }
        val urgeLoading by viewModel.urgeLoading.collectAsState()
        ShowProgressDialog(
            show = urgeLoading, text = "正在催更……", type = Z_TYPE.PAC_MAN
        )
    }
}

@Composable
private fun BuildTopDesc(remainCount: Int) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        backgroundColor = XhuColor.cardBackground,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Text(
                text = "因为作者精力有限，所以在此处列出 $appName 后续会实现的功能列表。",
                fontSize = 13.sp,
            )
            Text(
                text = "您可以在期望尽快实现的功能上点击 催更 按钮，让作者尽快知道哪些功能是大家需要的。",
                fontSize = 13.sp,
            )
            Text(
                text = buildAnnotatedString {
                    append("您(${DataHolder.mainUserName})当前剩余的催更次数为： ")
                    withStyle(
                        style = SpanStyle(
                            color = MaterialTheme.colors.primary,
                        )
                    ) {
                        append(remainCount.toString())
                    }
                    append(" 次")
                },
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun BuildItem(
    urgeItem: UrgeItem,
    showDetail: Boolean,
    onClick: () -> Unit,
    onUrge: (UrgeItem) -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .clickable(onClick = onClick),
        backgroundColor = XhuColor.cardBackground,
    ) {
        Row(
            modifier = Modifier
                .padding(8.dp)
                .animateContentSize(),
        ) {
            Column(
                modifier = Modifier
                    .weight(1F)
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (!urgeItem.complete) {
                    LinearProgressIndicator(
                        progress = urgeItem.rate / 100F,
                    )
                }
                val title = if (urgeItem.complete) "【已完成】${urgeItem.title}" else urgeItem.title
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                )
                if (showDetail) {
                    Text(
                        text = urgeItem.description.ifBlank { "暂无描述" },
                        fontSize = 14.sp,
                    )
                }
                Text(
                    text = "发布于 ${
                        LocalDateTime.ofInstant(
                            Instant.ofEpochMilli(urgeItem.createTime),
                            chinaZone
                        ).format(chinaDateTimeFormatter)
                    }",
                    fontSize = 12.sp,
                    modifier = Modifier
                        .fillMaxWidth(),
                    color = XhuColor.Common.grayText,
                )
                if (showDetail) {
                    Text(
                        text = "更新于 ${
                            LocalDateTime.ofInstant(
                                Instant.ofEpochMilli(urgeItem.updateTime),
                                chinaZone
                            ).format(chinaDateTimeFormatter)
                        }",
                        fontSize = 12.sp,
                        modifier = Modifier
                            .fillMaxWidth(),
                        color = XhuColor.Common.grayText,
                    )
                }
            }
            if (!urgeItem.complete) {
                Button(
                    enabled = !urgeItem.urged,
                    onClick = {
                        onUrge(urgeItem)
                    }) {
                    Row {
                        Icon(painter = XhuIcons.Profile.urge, contentDescription = null)
                        Text(text = "(${urgeItem.count})")
                    }
                }
            }
        }
    }
}