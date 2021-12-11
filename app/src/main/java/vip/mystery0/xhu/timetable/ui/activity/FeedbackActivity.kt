package vip.mystery0.xhu.timetable.ui.activity

import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.accompanist.insets.LocalWindowInsets
import com.google.accompanist.insets.navigationBarsWithImePadding
import com.google.accompanist.insets.rememberInsetsPaddingValues
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import vip.mystery0.xhu.timetable.base.BaseComposeActivity
import vip.mystery0.xhu.timetable.config.chinaZone
import vip.mystery0.xhu.timetable.model.response.Message
import vip.mystery0.xhu.timetable.ui.activity.feedback.SymbolAnnotationType
import vip.mystery0.xhu.timetable.ui.activity.feedback.UserInput
import vip.mystery0.xhu.timetable.ui.activity.feedback.messageFormatter
import vip.mystery0.xhu.timetable.ui.theme.XhuIcons
import vip.mystery0.xhu.timetable.utils.enDateFormatter
import vip.mystery0.xhu.timetable.utils.enTimeFormatter
import vip.mystery0.xhu.timetable.viewmodel.FeedbackViewModel
import vip.mystery0.xhu.timetable.viewmodel.WebSocketStatus
import java.time.Duration
import java.time.Instant

class FeedbackActivity : BaseComposeActivity(), KoinComponent {
    private val viewModel: FeedbackViewModel by viewModels()

    @Composable
    override fun BuildContent() {
        val loading by viewModel.loading.collectAsState()
        val lazyListState = rememberLazyListState()
        val scope = rememberCoroutineScope()

        val wsState by viewModel.wsStatus.collectAsState()

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
                    actions = {
                        IconButton(onClick = {
                            viewModel.connectWebSocket()
                        }) {
                            val icon = when (wsState.status) {
                                WebSocketStatus.CONNECTED -> XhuIcons.WsState.connected
                                WebSocketStatus.CONNECTING -> XhuIcons.WsState.connecting
                                WebSocketStatus.DISCONNECTED -> XhuIcons.WsState.disconnected
                                WebSocketStatus.FAILED -> XhuIcons.WsState.failed
                            }
                            Icon(
                                painter = icon,
                                contentDescription = null,
                            )
                        }
                    }
                )
            },
        ) { paddingValues ->
            SwipeRefresh(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize(),
                state = rememberSwipeRefreshState(loading.loading),
                onRefresh = { viewModel.loadLast20Message() },
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize(),
                ) {
                    LazyColumn(
                        reverseLayout = true,
                        contentPadding = rememberInsetsPaddingValues(
                            insets = LocalWindowInsets.current.statusBars,
                            additionalTop = 90.dp
                        ),
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
                            val nextDate = nextTime.atZone(chinaZone).toLocalDate()
                            val thisDate = thisTime.atZone(chinaZone).toLocalDate()

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
                                    DayHeader(dayString = thisDate.format(enDateFormatter))
                                }
                            } else if (Duration.between(nextTime, thisTime).toMinutes() > 5) {
                                item {
                                    DayHeader(
                                        dayString = thisTime.atZone(chinaZone)
                                            .format(enTimeFormatter)
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
                        // Use navigationBarsWithImePadding(), to move the input panel above both the
                        // navigation bar, and on-screen keyboard (IME)
                        modifier = Modifier.navigationBarsWithImePadding(),
                    )
                }
            }
        }
    }
}

@Composable
fun Message(
    modifier: Modifier,
    msg: Message,
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
    msg: Message,
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
            style = MaterialTheme.typography.caption,
            color = MaterialTheme.colors.onSurface
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
        color = MaterialTheme.colors.onSurface.copy(alpha = 0.12f)
    )
}

@Composable
fun ChatItemBubble(
    message: Message,
) {
    val backgroundBubbleColor = if (message.isMe) {
        MaterialTheme.colors.primary
    } else {
        MaterialTheme.colors.secondary
    }
    val shape = if (message.isMe) {
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
                message = message,
            )
        }
    }
}

@Composable
fun ClickableMessage(
    message: Message,
) {
    val uriHandler = LocalUriHandler.current

    val styledMessage = messageFormatter(
        text = message.content,
        primary = message.isMe
    )

    ClickableText(
        text = styledMessage,
        modifier = Modifier.padding(16.dp),
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

@Preview
@Composable
fun DayHeaderPrev() {
    DayHeader("Aug 6")
}