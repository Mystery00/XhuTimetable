package vip.mystery0.xhu.timetable.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.datetime.format
import org.koin.compose.viewmodel.koinViewModel
import vip.mystery0.xhu.timetable.base.HandleErrorMessage
import vip.mystery0.xhu.timetable.config.coroutine.safeLaunch
import vip.mystery0.xhu.timetable.config.store.Formatter
import vip.mystery0.xhu.timetable.model.ws.TextMessage
import vip.mystery0.xhu.timetable.ui.component.feedback.UserInput
import vip.mystery0.xhu.timetable.ui.component.feedback.messageFormatter
import vip.mystery0.xhu.timetable.ui.navigation.LocalNavController
import vip.mystery0.xhu.timetable.ui.theme.XhuIcons
import vip.mystery0.xhu.timetable.utils.asLocalDateTime
import vip.mystery0.xhu.timetable.utils.between
import vip.mystery0.xhu.timetable.viewmodel.FeedbackViewModel
import vip.mystery0.xhu.timetable.viewmodel.WebSocketStatus
import kotlin.time.Duration
import kotlin.time.Instant

@Composable
fun FeedbackScreen() {
    val viewModel = koinViewModel<FeedbackViewModel>()
    val navController = LocalNavController.current

    val loading by viewModel.loading.collectAsState()
    val lazyListState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    val wsState by viewModel.wsStatus.collectAsState()
    val adminStatus by viewModel.adminStatus.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.init()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    val (statusText, statusColor) = when {
                        wsState.status == WebSocketStatus.CONNECTING -> "正在连接…" to Color(0xFFFFA000)
                        wsState.status == WebSocketStatus.FAILED || wsState.status == WebSocketStatus.DISCONNECTED ->
                            "连接已断开 · 点击重连" to MaterialTheme.colorScheme.error
                        adminStatus.online -> "管理员在线" to Color(0xFF2E7D32)
                        else -> "已连接 · 留言即可收到回复" to MaterialTheme.colorScheme.primary
                    }
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .clickable(
                                enabled = wsState.status != WebSocketStatus.CONNECTED
                            ) {
                                viewModel.connectWebSocket()
                            }
                            .padding(horizontal = 8.dp, vertical = 2.dp),
                    ) {
                        Text(
                            text = "意见反馈",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp),
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(statusColor)
                            )
                            Text(
                                text = statusText,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            painter = XhuIcons.back,
                            contentDescription = "返回",
                        )
                    }
                },
                actions = {
                    if (wsState.status != WebSocketStatus.CONNECTED) {
                        IconButton(onClick = { viewModel.connectWebSocket() }) {
                            Icon(
                                imageVector = Icons.Rounded.Refresh,
                                contentDescription = "重新连接",
                            )
                        }
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
            val pullToRefreshState = rememberPullToRefreshState()
            PullToRefreshBox(
                state = pullToRefreshState,
                isRefreshing = loading.loading,
                onRefresh = { viewModel.loadLastMessage(10) },
                indicator = {
                    PullToRefreshDefaults.LoadingIndicator(
                        state = pullToRefreshState,
                        isRefreshing = loading.loading,
                        modifier = Modifier.align(Alignment.TopCenter)
                    )
                }
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    LazyColumn(
                        reverseLayout = true,
                        contentPadding = PaddingValues(vertical = 12.dp),
                        modifier = Modifier.weight(1f),
                        state = lazyListState,
                    ) {
                        val messages = viewModel.messageState.messages
                        for (index in messages.indices) {
                            val msg = messages[index]
                            val lastMsg = messages.getOrNull(index - 1)
                            val nextMsg = messages.getOrNull(index + 1)

                            val isFirstMessage = lastMsg?.isMe != msg.isMe
                            val isLastMessage = nextMsg?.isMe != msg.isMe
                            val nextTime = nextMsg?.sendTime ?: Instant.fromEpochMilliseconds(0L)
                            val thisTime = msg.sendTime
                            val nextDate = nextTime.asLocalDateTime().date
                            val thisDate = thisTime.asLocalDateTime().date

                            item {
                                Message(
                                    modifier = Modifier.fillParentMaxWidth(),
                                    msg = msg,
                                    isFirstMessage = isFirstMessage,
                                    isLastMessage = isLastMessage,
                                )
                            }
                            if (nextDate != thisDate) {
                                item {
                                    DayHeader(dayString = thisDate.format(Formatter.DATE))
                                }
                            } else if (Duration.between(nextTime, thisTime).inWholeHours > 1) {
                                item {
                                    DayHeader(
                                        dayString = thisTime.asLocalDateTime()
                                            .time
                                            .format(Formatter.TIME_NO_SECONDS)
                                    )
                                }
                            }
                        }
                    }
                    UserInput(
                        onMessageSent = { content ->
                            viewModel.sendMessage(content.trim())
                        },
                        resetScroll = {
                            scope.safeLaunch(Dispatchers.Main) {
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
        // 不再通过干扰性黑框 Toast 弹出“管理员下线”，已整合在顶栏状态副标题中
        LaunchedEffect(adminStatus.message) {
            if (adminStatus.message.isNotBlank()) {
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
    val spaceBetween = if (isLastMessage) modifier.padding(top = 6.dp) else modifier
    Row(modifier = spaceBetween) {
        TextMessage(
            msg = msg,
            isFirstMessage = isFirstMessage,
            modifier = Modifier
                .padding(horizontal = 14.dp)
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
        Spacer(modifier = Modifier.height(if (isFirstMessage) 8.dp else 4.dp))
    }
}

private val ReceiveChatBubbleShape = RoundedCornerShape(4.dp, 18.dp, 18.dp, 18.dp)
private val SendChatBubbleShape = RoundedCornerShape(18.dp, 18.dp, 4.dp, 18.dp)

@Composable
private fun DayHeader(dayString: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp, horizontal = 24.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        DayHeaderLine()
        Text(
            text = dayString,
            modifier = Modifier.padding(horizontal = 12.dp),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        DayHeaderLine()
    }
}

@Composable
private fun RowScope.DayHeaderLine() {
    HorizontalDivider(
        modifier = Modifier.weight(1f),
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f),
    )
}

@Composable
private fun ChatItemBubble(textMessage: TextMessage) {
    val backgroundBubbleColor = if (textMessage.isMe) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.surfaceContainerHigh
    }
    val shape = if (textMessage.isMe) SendChatBubbleShape else ReceiveChatBubbleShape

    Surface(
        color = backgroundBubbleColor,
        shape = shape,
        modifier = Modifier.widthIn(max = 290.dp),
    ) {
        ClickableMessage(textMessage = textMessage)
    }
}

@Composable
fun ClickableMessage(textMessage: TextMessage) {
    val cleanText = textMessage.content.trimEnd('\n', '\r')
    val styledMessage = messageFormatter(
        text = cleanText,
        primary = textMessage.isMe
    )
    val textColor = if (textMessage.isMe) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    SelectionContainer {
        BasicText(
            text = styledMessage,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            style = MaterialTheme.typography.bodyMedium.copy(color = textColor),
        )
    }
}
