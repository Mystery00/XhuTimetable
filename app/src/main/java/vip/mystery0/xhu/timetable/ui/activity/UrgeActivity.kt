package vip.mystery0.xhu.timetable.ui.activity

import androidx.activity.viewModels
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import vip.mystery0.xhu.timetable.appName
import vip.mystery0.xhu.timetable.base.BasePageComposeActivity
import vip.mystery0.xhu.timetable.model.LottieLoadingType
import vip.mystery0.xhu.timetable.model.response.UrgeItem
import vip.mystery0.xhu.timetable.ui.theme.XhuColor
import vip.mystery0.xhu.timetable.ui.theme.XhuIcons
import vip.mystery0.xhu.timetable.utils.formatChinaDateTime
import vip.mystery0.xhu.timetable.viewmodel.UrgeViewModel

class UrgeActivity : BasePageComposeActivity() {
    private val viewModel: UrgeViewModel by viewModels()

    @Composable
    override fun BuildContent() {
        val pager = viewModel.pageState.collectAndHandleState(viewModel::handleLoadState)

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
            val refreshing by viewModel.refreshing.collectAsState()
            val userName by viewModel.userName.collectAsState()
            val remainCount by viewModel.remainCount.collectAsState()
            var expandItemIndex by remember { mutableStateOf(-1) }
            BuildPaging(
                paddingValues = paddingValues,
                pager = pager,
                refreshing = refreshing,
                listContent = {
                    item {
                        BuildTopDesc(userName = userName, remainCount = remainCount)
                    }
                    items(
                        pager.itemCount,
                        key = { index -> pager[index]?.urgeId ?: index },
                    ) { index ->
                        val item = pager[index] ?: return@items
                        BuildItem(
                            urgeItem = item,
                            expandItemIndex == index,
                            onClick = {
                                expandItemIndex =
                                    if (expandItemIndex != index) index else -1
                            }) {
                            viewModel.urge(it.urgeId)
                        }
                    }
                },
            )
        }

        val errorMessage by viewModel.errorMessage.collectAsState()
        if (errorMessage.second.isNotBlank()) {
            errorMessage.second.toast(true)
        }
        val urgeLoading by viewModel.urgeLoading.collectAsState()
        ShowProgressDialog(
            show = urgeLoading,
            text = "正在催更……",
            type = LottieLoadingType.SETTING,
        )
    }
}

@Composable
private fun BuildTopDesc(userName: String, remainCount: Int) {
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
                text = "如果您想要的功能不在以下列表中，可以通过 意见反馈 功能联系作者，也可以加入交流反馈群进行反馈。",
                fontSize = 13.sp,
            )
            Text(
                text = buildAnnotatedString {
                    append("您(${userName})当前剩余的催更次数为： ")
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
                    text = "发布于 ${urgeItem.createTime.formatChinaDateTime()}",
                    fontSize = 12.sp,
                    modifier = Modifier
                        .fillMaxWidth(),
                    color = XhuColor.Common.grayText,
                )
                if (showDetail) {
                    Text(
                        text = "更新于 ${urgeItem.updateTime.formatChinaDateTime()}",
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