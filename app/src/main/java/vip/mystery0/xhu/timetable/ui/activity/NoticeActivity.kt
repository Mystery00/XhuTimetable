package vip.mystery0.xhu.timetable.ui.activity

import android.util.Log
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.paging.compose.collectAsLazyPagingItems
import vip.mystery0.xhu.timetable.base.BasePageComposeActivity
import vip.mystery0.xhu.timetable.loadInBrowser
import vip.mystery0.xhu.timetable.model.event.EventType
import vip.mystery0.xhu.timetable.model.event.UIEvent
import vip.mystery0.xhu.timetable.model.response.NoticeResponse
import vip.mystery0.xhu.timetable.ui.theme.XhuColor
import vip.mystery0.xhu.timetable.ui.theme.XhuFonts
import vip.mystery0.xhu.timetable.ui.theme.XhuIcons
import vip.mystery0.xhu.timetable.utils.formatChinaDateTime
import vip.mystery0.xhu.timetable.viewmodel.NoticeViewModel

class NoticeActivity : BasePageComposeActivity() {
    private val viewModel: NoticeViewModel by viewModels()
    private val regex =
        Regex("(https?)://[-A-Za-z0-9+&@#/%?=~_|!:,.;]+[-A-Za-z0-9+&@#/%=~_|]")

    @Composable
    override fun BuildContent() {
        val pager = viewModel.pageState.collectAsLazyPagingItems()

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
            BuildPaging(
                paddingValues = paddingValues,
                pager = pager,
                refreshing = refreshing,
                itemContent = { item ->
                    item?.let {
                        BuildItem(it)
                    }
                },
            )
        }
    }

    @Composable
    fun BuildItem(notice: NoticeResponse) {
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
                    text = "发布于 ${notice.createTime.formatChinaDateTime()}",
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
                style = TextStyle(
                    fontFamily = XhuFonts.DEFAULT,
                ),
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

    override fun onStop() {
        eventBus.post(UIEvent(EventType.UPDATE_NOTICE_CHECK))
        super.onStop()
    }
}