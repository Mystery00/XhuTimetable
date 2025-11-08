package vip.mystery0.xhu.timetable.repository

import co.touchlab.kermit.Logger
import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.webSocketSession
import io.ktor.client.request.header
import io.ktor.client.request.url
import io.ktor.websocket.Frame
import io.ktor.websocket.WebSocketSession
import io.ktor.websocket.close
import io.ktor.websocket.readText
import org.koin.core.component.inject
import org.koin.core.qualifier.named
import vip.mystery0.xhu.timetable.api.FeedbackApi
import vip.mystery0.xhu.timetable.base.BaseDataRepo
import vip.mystery0.xhu.timetable.base.appVersionCode
import vip.mystery0.xhu.timetable.base.appVersionName
import vip.mystery0.xhu.timetable.base.publicDeviceId
import vip.mystery0.xhu.timetable.config.store.UserStore
import vip.mystery0.xhu.timetable.config.store.UserStore.withAutoLoginOnce
import vip.mystery0.xhu.timetable.config.store.getCacheStore
import vip.mystery0.xhu.timetable.model.ws.TextMessage
import vip.mystery0.xhu.timetable.model.ws.parseMessage
import vip.mystery0.xhu.timetable.module.HTTP_CLIENT_WS
import vip.mystery0.xhu.timetable.viewmodel.WebSocketState
import vip.mystery0.xhu.timetable.viewmodel.WebSocketStatus

object FeedbackRepo : BaseDataRepo {
    private val logger = Logger.withTag("FeedbackRepo")
    private val feedbackApi: FeedbackApi by inject()
    private val wsClient: HttpClient by inject(named(HTTP_CLIENT_WS))

    suspend fun initWebSocket(
        statusConsumer: (WebSocketState) -> Unit,
    ): WebSocketSession {
        statusConsumer(WebSocketState(WebSocketStatus.CONNECTING))
        val mainUser = UserStore.mainUser()
        val session = wsClient.webSocketSession {
            url("wss://ws.api.mystery0.vip/ws?sessionToken=${mainUser.token}&wrap=true")
            header("deviceId", publicDeviceId())
            header("clientVersionName", appVersionName())
            header("clientVersionCode", appVersionCode())
        }
        statusConsumer(WebSocketState(WebSocketStatus.CONNECTED))
        return session
    }

    suspend fun handleMessage(
        session: WebSocketSession,
        messageConsumer: (TextMessage) -> Unit,
        systemMessageConsumer: (Any) -> Unit,
        statusConsumer: (WebSocketState) -> Unit,
    ) {
        try {
            val mainUser = UserStore.mainUser()
            while (true) {
                val frame = session.incoming.receive()
                when (frame) {
                    is Frame.Close -> {
                        statusConsumer(
                            WebSocketState(
                                WebSocketStatus.DISCONNECTED,
                                frame.data.decodeToString()
                            )
                        )
                        break
                    }

                    is Frame.Text -> {
                        val msg = parseMessage(frame.readText())
                        when (msg) {
                            is TextMessage -> {
                                messageConsumer(msg.copy(isMe = mainUser.studentId == msg.fromUserId))
                            }

                            else -> {
                                systemMessageConsumer(msg)
                            }
                        }
                    }

                    else -> continue
                }

                val text = session.incoming.receive() as? Frame.Text
                if (text == null) {
                    continue
                }
                val msg = parseMessage(text.readText())
                when (msg) {
                    is TextMessage -> {
                        messageConsumer(msg.copy(isMe = mainUser.studentId == msg.fromUserId))
                    }

                    else -> {
                        systemMessageConsumer(msg)
                    }
                }
            }
        } catch (e: Exception) {
            logger.e("onFailure", e)
            statusConsumer(
                WebSocketState(
                    WebSocketStatus.FAILED,
                    e.message ?: "异常断开，请重新连接"
                )
            )
        } finally {
            session.close()
            session.incoming.cancel()
        }
    }

    suspend fun loadLastMessage(lastId: Long, size: Int): List<TextMessage> {
        val mainUser = UserStore.mainUser()
        val list = mainUser.withAutoLoginOnce {
            feedbackApi.pullMessage(it, lastId, size)
        }
        return list.map {
            it.copy(isMe = mainUser.studentId == it.fromUserId)
        }.reversed()
    }

    suspend fun checkUnReadFeedback(): Boolean {
        if (!isOnline) {
            return false
        }

        val firstId = getCacheStore { firstFeedbackMessageId }
        return mainUser().withAutoLoginOnce {
            feedbackApi.checkMessage(it, firstId)
        }.newResult
    }
}