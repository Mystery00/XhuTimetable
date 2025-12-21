package vip.mystery0.xhu.timetable.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.OpenInBrowser
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.datetime.format
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel
import vip.mystery0.xhu.timetable.model.response.NoticeActionType
import vip.mystery0.xhu.timetable.model.response.NoticeResponse
import vip.mystery0.xhu.timetable.model.response.parseNoticeActionType
import vip.mystery0.xhu.timetable.ui.component.BuildPaging
import vip.mystery0.xhu.timetable.ui.component.PageItemLayout
import vip.mystery0.xhu.timetable.ui.component.StateScreen
import vip.mystery0.xhu.timetable.ui.component.collectAndHandleState
import vip.mystery0.xhu.timetable.ui.navigation.LocalNavController
import vip.mystery0.xhu.timetable.ui.theme.XhuFonts
import vip.mystery0.xhu.timetable.ui.theme.XhuIcons
import vip.mystery0.xhu.timetable.utils.asLocalDateTime
import vip.mystery0.xhu.timetable.utils.chinaDateTime
import vip.mystery0.xhu.timetable.utils.copyToClipboard
import vip.mystery0.xhu.timetable.viewmodel.NoticeViewModel
import xhutimetable.composeapp.generated.resources.Res
import xhutimetable.composeapp.generated.resources.state_no_data

@Composable
fun NoticeScreen() {
    val viewModel = koinViewModel<NoticeViewModel>()

    val navController = LocalNavController.current!!

    val pager = viewModel.pageState.collectAndHandleState(viewModel::handleLoadState)

    LaunchedEffect(Unit) {
        viewModel.loadList()
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
            emptyState = {
                val loadingErrorMessage by viewModel.loadingErrorMessage.collectAsState()
                StateScreen(
                    title = loadingErrorMessage ?: "暂无数据",
                    buttonText = "再查一次",
                    imageRes = painterResource(Res.drawable.state_no_data),
                    verticalArrangement = Arrangement.Top,
                    onButtonClick = {
                        viewModel.loadList()
                    }
                )
            },
        )
    }
}

@Composable
fun BuildItem(notice: NoticeResponse) {
    val uriHandler = LocalUriHandler.current
    PageItemLayout(
        header = {
            Text(notice.title)
        },
        content = {
            AnnotatedClickableText(
                text = notice.content,
            )
            Text(
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.End,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.outline,
                text = "发布时间：${notice.createTime.asLocalDateTime().format(chinaDateTime)}"
            )
        },
        footer = if (notice.actions.isEmpty()) null else {
            {
                notice.actions.forEach {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = it.text,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.weight(1F),
                            fontSize = 16.sp,
                        )
                        when (parseNoticeActionType(it.actionType)) {
                            NoticeActionType.COPY -> {
                                ActionButton(
                                    text = "复制",
                                    imageVector = Icons.Rounded.ContentCopy,
                                    onClick = {
                                        copyToClipboard(it.metadata)
                                    }
                                )
                            }

                            NoticeActionType.OPEN_URI -> {
                                ActionButton(
                                    text = "访问",
                                    imageVector = Icons.Rounded.OpenInBrowser,
                                    onClick = {
                                        uriHandler.openUri(it.metadata)
                                    }
                                )
                            }

                            else -> {
                            }
                        }
                    }
                }
            }
        }
    )
}

@Composable
fun ActionButton(
    text: String,
    imageVector: ImageVector,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .clickable(
                interactionSource = MutableInteractionSource(),
                indication = null,
                onClick = onClick
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = text,
            color = MaterialTheme.colorScheme.outline,
            fontSize = 14.sp,
        )
        Spacer(modifier = Modifier.width(2.dp))
        Icon(
            modifier = Modifier.size(20.dp),
            imageVector = imageVector,
            contentDescription = null,
        )
    }
}

private val regex =
    Regex("""(`[^`]+`)|(\*.+?\*)|(_.+?_)|(~.+?~)""")

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

        else -> {
            append(matchResult.value)
        }
    }
}