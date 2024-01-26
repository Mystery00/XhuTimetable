package vip.mystery0.xhu.timetable.repository

import android.util.Log
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapter
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.koin.core.component.inject
import vip.mystery0.xhu.timetable.api.FeedbackApi
import vip.mystery0.xhu.timetable.base.BaseDataRepo
import vip.mystery0.xhu.timetable.config.store.UserStore
import vip.mystery0.xhu.timetable.config.store.UserStore.withAutoLoginOnce
import vip.mystery0.xhu.timetable.config.store.getCacheStore
import vip.mystery0.xhu.timetable.model.ws.BaseMessage
import vip.mystery0.xhu.timetable.model.ws.MessageAdapter
import vip.mystery0.xhu.timetable.model.ws.SystemMessage
import vip.mystery0.xhu.timetable.model.ws.TextMessage
import vip.mystery0.xhu.timetable.module.registerAdapter
import vip.mystery0.xhu.timetable.viewmodel.WebSocketState
import vip.mystery0.xhu.timetable.viewmodel.WebSocketStatus
import java.util.concurrent.TimeUnit

object FeedbackRepo : BaseDataRepo {
    private const val TAG = "FeedbackRepo"
    private val feedbackApi: FeedbackApi by inject()

    @OptIn(ExperimentalStdlibApi::class)
    private val jsonAdapter = Moshi.Builder()
        .registerAdapter()
        .add(BaseMessage::class.java, MessageAdapter())
        .build()
        .adapter<BaseMessage>()

    suspend fun initWebSocket(
        messageConsumer: (TextMessage) -> Unit,
        systemMessageConsumer: (SystemMessage) -> Unit,
        statusConsumer: (WebSocketState) -> Unit,
    ): WebSocket {
        statusConsumer(WebSocketState(WebSocketStatus.CONNECTING))
        val mainUser = UserStore.mainUser()
        val url = "wss://ws.api.mystery0.vip/ws?sessionToken=${mainUser.token}"
        val request = Request.Builder()
            .url(url)
            .build()
        val webSocketClient = OkHttpClient.Builder()
            .pingInterval(5, TimeUnit.SECONDS)
            .build()
        return webSocketClient.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                super.onOpen(webSocket, response)
                statusConsumer(WebSocketState(WebSocketStatus.CONNECTED))
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                super.onMessage(webSocket, text)
                val msg = jsonAdapter.fromJson(text) ?: return
                when (msg) {
                    is TextMessage -> {
                        msg.generate(mainUser.studentId)
                        messageConsumer(msg)
                    }

                    is SystemMessage -> {
                        systemMessageConsumer(msg)
                    }
                }
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                super.onClosed(webSocket, code, reason)
                statusConsumer(WebSocketState(WebSocketStatus.DISCONNECTED, reason))
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                super.onFailure(webSocket, t, response)
                Log.e(TAG, "onFailure: ", t)
                statusConsumer(
                    WebSocketState(
                        WebSocketStatus.FAILED,
                        t.message ?: "异常断开，请重新连接"
                    )
                )
            }
        })
    }

    suspend fun loadLastMessage(lastId: Long, size: Int): List<TextMessage> {
        val mainUser = UserStore.mainUser()
        val list = mainUser.withAutoLoginOnce {
            feedbackApi.pullMessage(it, lastId, size)
        }
        list.forEach { it.generate(mainUser.studentId) }
        return list.reversed()
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