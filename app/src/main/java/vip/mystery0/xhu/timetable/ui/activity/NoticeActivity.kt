package vip.mystery0.xhu.timetable.ui.activity

import android.content.ClipData
import android.content.ClipboardManager
import android.util.Log
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ColorScheme
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
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.koin.core.component.inject
import vip.mystery0.xhu.timetable.base.BasePageComposeActivity
import vip.mystery0.xhu.timetable.loadInBrowser
import vip.mystery0.xhu.timetable.model.response.NoticeResponse
import vip.mystery0.xhu.timetable.ui.activity.feedback.StringAnnotation
import vip.mystery0.xhu.timetable.ui.activity.feedback.SymbolAnnotation
import vip.mystery0.xhu.timetable.ui.theme.XhuColor
import vip.mystery0.xhu.timetable.ui.theme.XhuFonts
import vip.mystery0.xhu.timetable.ui.theme.XhuIcons
import vip.mystery0.xhu.timetable.utils.formatChinaDateTime
import vip.mystery0.xhu.timetable.viewmodel.NoticeViewModel

class NoticeActivity : BasePageComposeActivity() {
    private val regex = Regex("(https?://[^\\s\\t\\n]+)|(\\d{4}\\d+)")

    private val viewModel: NoticeViewModel by viewModels()
    private val clipboardManager: ClipboardManager by inject()

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
        val tokens = regex.findAll(text)
        val annotatedText = buildAnnotatedString {
            var cursorPosition = 0
            for (token in tokens) {
                append(text.slice(cursorPosition until token.range.first))
                val (annotatedString, stringAnnotation) = getSymbolAnnotation(
                    token,
                    MaterialTheme.colorScheme
                )
                append(annotatedString)
                if (stringAnnotation != null) {
                    val (item, start, end, tag) = stringAnnotation
                    addStringAnnotation(tag = tag, start = start, end = end, annotation = item)
                }
                cursorPosition = token.range.last + 1
            }

            if (!tokens.none()) {
                append(text.slice(cursorPosition..text.lastIndex))
            } else {
                append(text)
            }
        }

        SelectionContainer {
            ClickableText(
                modifier = modifier,
                text = annotatedText,
                style = TextStyle(
                    fontFamily = XhuFonts.DEFAULT,
                    color = MaterialTheme.colorScheme.onSurface
                ),
                onClick = { offset ->
                    annotatedText.getStringAnnotations(start = offset, end = offset)
                        .firstOrNull()
                        ?.let { annotation ->
                            when (annotation.tag) {
                                "URL" -> {
                                    Log.d("Clicked URL", annotation.item)
                                    loadInBrowser(annotation.item)
                                }

                                "NUMBER" -> {
                                    Log.d("Clicked Number", annotation.item)
                                    clipboardManager.setPrimaryClip(
                                        ClipData.newPlainText("Number", annotation.item)
                                    )
                                }
                            }
                        }
                }
            )
        }
    }

    private fun getSymbolAnnotation(
        matchResult: MatchResult,
        colorScheme: ColorScheme,
    ): SymbolAnnotation {
        return when (matchResult.value.first()) {
            'h' -> SymbolAnnotation(
                AnnotatedString(
                    text = matchResult.value,
                    spanStyle = SpanStyle(
                        color = colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                ),
                StringAnnotation(
                    item = matchResult.value,
                    start = matchResult.range.first,
                    end = matchResult.range.last,
                    tag = "URL"
                )
            )

            else -> SymbolAnnotation(
                AnnotatedString(
                    text = matchResult.value,
                    spanStyle = SpanStyle(
                        color = colorScheme.tertiary,
                        fontWeight = FontWeight.Bold
                    )
                ),
                StringAnnotation(
                    item = matchResult.value,
                    start = matchResult.range.first,
                    end = matchResult.range.last,
                    tag = "NUMBER"
                )
            )
        }
    }
}