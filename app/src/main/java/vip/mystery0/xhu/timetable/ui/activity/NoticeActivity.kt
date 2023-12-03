package vip.mystery0.xhu.timetable.ui.activity

import android.util.Log
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
import vip.mystery0.xhu.timetable.base.BasePageComposeActivity
import vip.mystery0.xhu.timetable.loadInBrowser
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

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun BuildContent() {
        val pager = viewModel.pageState.collectAndHandleState(viewModel::handleLoadState)

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(text = title.toString()) },
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
                key = { index -> pager[index]?.noticeId ?: index },
                itemContent = @Composable { item ->
                    BuildItem(item)
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
            colors = CardDefaults.cardColors(
                containerColor = XhuColor.cardBackground,
            ),
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
                    color = MaterialTheme.colorScheme.outline,
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
                    style = SpanStyle(color = MaterialTheme.colorScheme.onBackground),
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
}