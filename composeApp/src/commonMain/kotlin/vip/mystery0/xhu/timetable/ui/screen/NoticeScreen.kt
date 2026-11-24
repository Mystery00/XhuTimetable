package vip.mystery0.xhu.timetable.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.OpenInBrowser
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.FilledTonalButton
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
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.datetime.format
import multiplatform.network.cmptoast.showToast
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
    val navController = LocalNavController.current
    val pager = viewModel.pageState.collectAndHandleState(viewModel::handleLoadState)

    LaunchedEffect(Unit) {
        viewModel.loadList()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "通知公告",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            painter = XhuIcons.back,
                            contentDescription = "返回",
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
            itemContent = { item ->
                BuildItem(item)
            },
            emptyState = {
                val loadingErrorMessage by viewModel.loadingErrorMessage.collectAsState()
                StateScreen(
                    title = loadingErrorMessage ?: "暂无通知公告",
                    buttonText = "重新加载",
                    imageRes = painterResource(Res.drawable.state_no_data),
                    onButtonClick = { viewModel.loadList() }
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
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(notice.title)
                Text(
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    text = "发布于 ${notice.createTime.asLocalDateTime().format(chinaDateTime)}",
                )
            }
        },
        content = {
            AnnotatedClickableText(text = notice.content)
        },
        footer = if (notice.actions.isEmpty()) null else {
            {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    notice.actions.forEach { action ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Text(
                                text = action.text,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.weight(1f),
                            )
                            when (parseNoticeActionType(action.actionType)) {
                                NoticeActionType.COPY -> {
                                    FilledTonalButton(
                                        onClick = {
                                            copyToClipboard(action.metadata)
                                            showToast("已复制到剪贴板")
                                        },
                                        shape = RoundedCornerShape(10.dp),
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.ContentCopy,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp),
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("复制")
                                    }
                                }

                                NoticeActionType.OPEN_URI -> {
                                    FilledTonalButton(
                                        onClick = { uriHandler.openUri(action.metadata) },
                                        shape = RoundedCornerShape(10.dp),
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.OpenInBrowser,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp),
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("访问")
                                    }
                                }

                                else -> {}
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
        modifier = Modifier.clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = text,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.labelMedium,
        )
        Spacer(modifier = Modifier.width(4.dp))
        Icon(
            modifier = Modifier.size(18.dp),
            imageVector = imageVector,
            contentDescription = null,
        )
    }
}

private val regex = Regex("""(`[^`]+`)|(\*.+?\*)|(_.+?_)|(~.+?~)""")

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
            style = MaterialTheme.typography.bodyMedium.copy(
                color = MaterialTheme.colorScheme.onSurface,
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
                    color = colors.onPrimaryContainer,
                    baselineShift = BaselineShift(0.1f),
                )
            ) {
                append(matchResult.value.trim('`'))
            }
        }

        else -> append(matchResult.value)
    }
}
