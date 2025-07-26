package vip.mystery0.xhu.timetable.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.datetime.format
import org.koin.compose.viewmodel.koinViewModel
import vip.mystery0.xhu.timetable.model.response.NoticeResponse
import vip.mystery0.xhu.timetable.ui.component.BuildPaging
import vip.mystery0.xhu.timetable.ui.component.collectAndHandleState
import vip.mystery0.xhu.timetable.ui.navigation.LocalNavController
import vip.mystery0.xhu.timetable.ui.theme.XhuColor
import vip.mystery0.xhu.timetable.ui.theme.XhuFonts
import vip.mystery0.xhu.timetable.ui.theme.XhuIcons
import vip.mystery0.xhu.timetable.utils.asLocalDateTime
import vip.mystery0.xhu.timetable.utils.chinaDateTime
import vip.mystery0.xhu.timetable.utils.copyToClipboard
import vip.mystery0.xhu.timetable.viewmodel.NoticeViewModel

@Composable
fun NoticeScreen() {
    val viewModel = koinViewModel<NoticeViewModel>()

    val navController = LocalNavController.current!!

    val pager = viewModel.pageState.collectAndHandleState(viewModel::handleLoadState)

    LaunchedEffect(Unit) {
        viewModel.init()
    }
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = "通知公告") },
                navigationIcon = {
                    IconButton(onClick = {
                        navController.popBackStack()
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
                text = "发布于 ${notice.createTime.asLocalDateTime().format(chinaDateTime)}",
                fontSize = 12.sp,
                modifier = Modifier
                    .fillMaxWidth(),
                textAlign = TextAlign.End,
                color = MaterialTheme.colorScheme.outline,
            )
        }
    }
}

private val regex =
    Regex("""(https?://[^\s\t\n]+)|(`[^`]+`)|(\*.+\*)|(_.+_)|(~.+~)|(\d{4}\d+)""")

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

            getSymbolAnnotation(
                matchResult = token,
                colors = MaterialTheme.colorScheme,
            )

            cursorPosition = token.range.last + 1
        }

        if (!tokens.none()) {
            append(text.slice(cursorPosition..text.lastIndex))
        } else {
            append(text)
        }
    }

    SelectionContainer {
        BasicText(
            modifier = modifier,
            text = annotatedText,
            style = TextStyle(
                fontFamily = XhuFonts.DEFAULT,
                color = MaterialTheme.colorScheme.onSurface
            ),
        )
    }
}

private fun AnnotatedString.Builder.getSymbolAnnotation(
    matchResult: MatchResult,
    colors: ColorScheme,
) {
    when (matchResult.value.first()) {
        '*' -> {
            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                append(matchResult.value.trim('*'))
            }
        }

        '_' -> {
            withStyle(style = SpanStyle(fontStyle = FontStyle.Italic)) {
                append(matchResult.value.trim('_'))
            }
        }

        '~' -> {
            withStyle(style = SpanStyle(textDecoration = TextDecoration.LineThrough)) {
                append(matchResult.value.trim('~'))
            }
        }

        '`' -> {
            withStyle(
                style = SpanStyle(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    background = colors.primaryContainer,
                    baselineShift = BaselineShift(0.2f)
                )
            ) {
                append(matchResult.value.trim('`'))
            }
        }

        'h' -> {
            withLink(
                link = LinkAnnotation.Url(matchResult.value),
            ) {
                withStyle(
                    style = SpanStyle(
                        color = colors.primary,
                        fontWeight = FontWeight.Bold,
                    )
                ) {
                    append(matchResult.value)
                }
            }
        }

        else -> {
            withLink(
                LinkAnnotation.Clickable(
                    tag = "NUMBER",
                    linkInteractionListener = {
                        copyToClipboard(matchResult.value)
                    }
                )
            ) {
                withStyle(
                    style = SpanStyle(
                        color = colors.tertiary,
                        fontWeight = FontWeight.Bold,
                    )
                ) {
                    append(matchResult.value)
                }
            }
        }
    }
}