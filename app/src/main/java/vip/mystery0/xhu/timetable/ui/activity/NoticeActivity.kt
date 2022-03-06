package vip.mystery0.xhu.timetable.ui.activity

import android.util.Log
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.placeholder.PlaceholderHighlight
import com.google.accompanist.placeholder.material.placeholder
import com.google.accompanist.placeholder.material.shimmer
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import vip.mystery0.xhu.timetable.base.BaseComposeActivity
import vip.mystery0.xhu.timetable.config.chinaZone
import vip.mystery0.xhu.timetable.loadInBrowser
import vip.mystery0.xhu.timetable.model.entity.Notice
import vip.mystery0.xhu.timetable.ui.theme.XhuColor
import vip.mystery0.xhu.timetable.ui.theme.XhuIcons
import vip.mystery0.xhu.timetable.utils.chinaDateTimeFormatter
import vip.mystery0.xhu.timetable.viewmodel.NoticeViewModel
import java.time.Instant
import java.time.LocalDateTime

class NoticeActivity : BaseComposeActivity() {
    private val viewModel: NoticeViewModel by viewModels()
    private val regex =
        Regex("(https?)://[-A-Za-z0-9+&@#/%?=~_|!:,.;]+[-A-Za-z0-9+&@#/%=~_|]")

    @Composable
    override fun BuildContent() {
        val noticeListState by viewModel.noticeListState.collectAsState()
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
                state = rememberSwipeRefreshState(noticeListState.loading),
                onRefresh = { viewModel.loadNoticeList() },
            ) {
                val list = noticeListState.noticeList
                if (noticeListState.loading || list.isNotEmpty()) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(XhuColor.Common.grayBackground),
                        contentPadding = PaddingValues(4.dp),
                    ) {
                        if (noticeListState.loading) {
                            items(3) {
                                BuildLoadingItem()
                            }
                        } else {
                            if (list.isNotEmpty()) {
                                items(list.size) { index ->
                                    BuildItem(notice = list[index])
                                }
                            }
                        }
                    }
                } else {
                    BuildNoDataLayout()
                }
            }
        }
        if (noticeListState.errorMessage.isNotBlank()) {
            noticeListState.errorMessage.toast(true)
        }
    }

    @Composable
    fun BuildItem(notice: Notice) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            backgroundColor = XhuColor.cardBackground,
        ) {
            Column(
                modifier = Modifier.padding(8.dp)
            ) {
                Text(
                    text = notice.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                )
                AnnotatedClickableText(
                    text = notice.content,
                )
                Text(
                    text = "发布于 ${
                        LocalDateTime.ofInstant(
                            Instant.ofEpochMilli(notice.createTime),
                            chinaZone
                        ).format(chinaDateTimeFormatter)
                    }",
                    fontSize = 12.sp,
                    modifier = Modifier
                        .fillMaxWidth(),
                    textAlign = TextAlign.End,
                    color = XhuColor.Common.grayText,
                )
            }
        }
    }

    @Composable
    fun AnnotatedClickableText(
        text: String,
        modifier: Modifier = Modifier,
    ) {
        val result = regex.findAll(text).toList()
        val split = text.split(regex)
        val annotatedText = buildAnnotatedString {
            split.forEachIndexed { index, s ->
                withStyle(
                    style = SpanStyle(color = MaterialTheme.colors.onBackground),
                ) {
                    append(s)
                }
                if (result.size > index) {
                    pushStringAnnotation(
                        tag = "URL",
                        annotation = result[index].value
                    )
                    withStyle(
                        style = SpanStyle(
                            color = Color.Blue,
                            fontWeight = FontWeight.Bold
                        )
                    ) {
                        append(result[index].value)
                    }
                    pop()
                }
            }
        }

        SelectionContainer {
            ClickableText(
                modifier = modifier,
                text = annotatedText,
                onClick = { offset ->
                    annotatedText.getStringAnnotations(
                        tag = "URL",
                        start = offset,
                        end = offset,
                    ).firstOrNull()?.let { annotation ->
                        Log.d("Clicked URL", annotation.item)
                        loadInBrowser(annotation.item)
                    }
                }
            )
        }
    }
}

@Composable
private fun BuildLoadingItem() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .placeholder(
                visible = true,
                highlight = PlaceholderHighlight.shimmer(),
            )
    ) {
        Column(
            modifier = Modifier.padding(4.dp)
        ) {
            Text(
                text = "占位符标题",
            )
            Text(
                text = "公告内容，\n总共\n三行",
            )
            Text(
                text = "2021-10-01",
                fontSize = 12.sp,
            )
        }
    }
}