package vip.mystery0.xhu.timetable.ui.activity

import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import vip.mystery0.xhu.timetable.base.BaseComposeActivity
import vip.mystery0.xhu.timetable.config.store.Formatter
import vip.mystery0.xhu.timetable.model.ws.TextMessage
import vip.mystery0.xhu.timetable.ui.activity.feedback.SymbolAnnotationType
import vip.mystery0.xhu.timetable.ui.activity.feedback.UserInput
import vip.mystery0.xhu.timetable.ui.activity.feedback.messageFormatter
import vip.mystery0.xhu.timetable.ui.theme.XhuIcons
import vip.mystery0.xhu.timetable.viewmodel.FeedbackViewModel
import vip.mystery0.xhu.timetable.viewmodel.WebSocketStatus
import java.time.Duration
import java.time.Instant

class FeedbackActivity : BaseComposeActivity(), KoinComponent {
    private val viewModel: FeedbackViewModel by viewModels()

    @OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
    @Composable
    override fun BuildContent() {
        val loading by viewModel.loading.collectAsState()
        val lazyListState = rememberLazyListState()
        val scope = rememberCoroutineScope()

        val wsState by viewModel.wsStatus.collectAsState()
        val adminStatus by viewModel.adminStatus.collectAsState()

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
                    actions = {
                        if (adminStatus.online) {
                            IconButton(onClick = {
                                toastString("管理员当前正在线上，发送消息管理员可以实时接收到")
                            }) {
                                Icon(
                                    painter = XhuIcons.WsState.adminOnline,
                                    contentDescription = null,
                                    tint = Color.Unspecified,
                                )
                            }
                        }
                        IconButton(onClick = {
                            viewModel.connectWebSocket()
                        }) {
                            val icon = when (wsState.status) {
                                WebSocketStatus.CONNECTED -> XhuIcons.WsState.connected
                                WebSocketStatus.CONNECTING -> XhuIcons.WsState.connecting
                                WebSocketStatus.DISCONNECTED -> XhuIcons.WsState.disconnected
                                WebSocketStatus.FAILED -> XhuIcons.WsState.failed
                            }
                            val tint = when (wsState.status) {
                                WebSocketStatus.CONNECTED, WebSocketStatus.CONNECTING -> LocalContentColor.current.copy(
                                    alpha = LocalContentAlpha.current
                                )

                                WebSocketStatus.DISCONNECTED, WebSocketStatus.FAILED -> Color.Unspecified
                            }
                            Icon(
                                painter = icon,
                                contentDescription = null,
                                tint = tint,
                            )
                        }
                    }
                )
            },
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
            ) {
                val pullRefreshState = rememberPullRefreshState(
                    refreshing = loading.loading,
                    onRefresh = { viewModel.loadLastMessage(10) },
                )
                Box(modifier = Modifier.pullRefresh(pullRefreshState)) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        LazyColumn(
                            reverseLayout = true,
                            contentPadding = WindowInsets.statusBars.add(WindowInsets(top = 90.dp))
                                .asPaddingValues(),
                            modifier = Modifier.weight(1F),
                            state = lazyListState,
                        ) {
                            val messages = viewModel.messageState.messages
                            for (index in messages.indices) {
                                val msg = messages[index]
                                val lastMsg = messages.getOrNull(index - 1)
                                val nextMsg = messages.getOrNull(index + 1)

                                val isFirstMessage = lastMsg?.isMe != msg.isMe
                                val isLastMessage = nextMsg?.isMe != msg.isMe
                                val nextTime = nextMsg?.sendTime ?: Instant.ofEpochMilli(0L)
                                val thisTime = msg.sendTime
                                val nextDate = nextTime.atZone(Formatter.ZONE_CHINA).toLocalDate()
                                val thisDate = thisTime.atZone(Formatter.ZONE_CHINA).toLocalDate()

                                item {
                                    Message(
                                        modifier = Modifier.fillParentMaxWidth(),
                                        msg = msg,
                                        isFirstMessage = isFirstMessage,
                                        isLastMessage = isLastMessage,
                                    )
                                }
                                if (!nextDate.equals(thisDate)) {
                                    item {
                                        DayHeader(dayString = thisDate.format(Formatter.DATE))
                                    }
                                } else if (Duration.between(nextTime, thisTime).toHours() > 1) {
                                    item {
                                        DayHeader(
                                            dayString = thisTime.atZone(Formatter.ZONE_CHINA)
                                                .format(Formatter.TIME_NO_SECONDS)
                                        )
                                    }
                                }
                            }
                        }
                        UserInput(
                            onMessageSent = { content ->
                                viewModel.sendMessage(content)
                            },
                            resetScroll = {
                                scope.launch {
                                    lazyListState.scrollToItem(0)
                                }
                            },
                            modifier = Modifier.imePadding(),
                        )
                    }
                }
            }

            HandleErrorMessage(errorMessage = loading.errorMessage) {
                viewModel.clearLoadingErrorMessage()
            }
            HandleErrorMessage(errorMessage = wsState.errorMessage) {
                viewModel.clearWebSocketErrorMessage()
            }
            HandleErrorMessage(errorMessage = adminStatus.message) {
                viewModel.clearAdminOnlineMessage()
            }
        }
    }
}

@Composable
fun Message(
    modifier: Modifier,
    msg: TextMessage,
    isFirstMessage: Boolean,
    isLastMessage: Boolean,
) {
    val spaceBetween = if (isLastMessage) modifier.padding(top = 8.dp) else modifier
    Row(
        modifier = spaceBetween,
    ) {
        TextMessage(
            msg = msg,
            isFirstMessage = isFirstMessage,
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .weight(1f),
        )
    }
}

@Composable
fun TextMessage(
    msg: TextMessage,
    isFirstMessage: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = if (msg.isMe) Alignment.End else Alignment.Start,
    ) {
        ChatItemBubble(msg)
        if (isFirstMessage) {
            // Last bubble before next author
            Spacer(modifier = Modifier.height(8.dp))
        } else {
            // Between bubbles
            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}

private val ReceiveChatBubbleShape = RoundedCornerShape(4.dp, 20.dp, 20.dp, 20.dp)
private val SendChatBubbleShape = RoundedCornerShape(20.dp, 20.dp, 4.dp, 20.dp)

@Composable
fun DayHeader(dayString: String) {
    Row(
        modifier = Modifier
            .padding(vertical = 8.dp, horizontal = 16.dp)
            .height(16.dp)
    ) {
        DayHeaderLine()
        Text(
            text = dayString,
            modifier = Modifier.padding(horizontal = 16.dp),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.outline
        )
        DayHeaderLine()
    }
}

@Composable
private fun RowScope.DayHeaderLine() {
    Divider(
        modifier = Modifier
            .weight(1f)
            .align(Alignment.CenterVertically),
        color = MaterialTheme.colorScheme.surfaceColorAtElevation(6.dp)
    )
}

@Composable
fun ChatItemBubble(
    textMessage: TextMessage,
) {
    val backgroundBubbleColor = if (textMessage.isMe) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.secondaryContainer
    }
    val shape = if (textMessage.isMe) {
        SendChatBubbleShape
    } else {
        ReceiveChatBubbleShape
    }
    Column {
        Surface(
            color = backgroundBubbleColor,
            shape = shape
        ) {
            ClickableMessage(
                textMessage = textMessage,
            )
        }
    }
}

@Composable
fun ClickableMessage(
    textMessage: TextMessage,
) {
    val uriHandler = LocalUriHandler.current

    val styledMessage = messageFormatter(
        text = textMessage.content,
        primary = textMessage.isMe
    )
    val textColor = if (textMessage.isMe) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSecondaryContainer
    }

    SelectionContainer {
        ClickableText(
            text = styledMessage,
            modifier = Modifier.padding(16.dp),
            style = TextStyle.Default.copy(color = textColor),
            onClick = {
                styledMessage
                    .getStringAnnotations(start = it, end = it)
                    .firstOrNull()
                    ?.let { annotation ->
                        when (annotation.tag) {
                            SymbolAnnotationType.LINK.name -> uriHandler.openUri(annotation.item)
                            else -> Unit
                        }
                    }
            }
        )
    }
}