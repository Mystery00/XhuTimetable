package vip.mystery0.xhu.timetable.nativeui

import io.ktor.http.URLBuilder
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.*
import vip.mystery0.xhu.timetable.base.appVersionCode
import vip.mystery0.xhu.timetable.base.appVersionName
import vip.mystery0.xhu.timetable.base.publicDeviceId
import vip.mystery0.xhu.timetable.config.store.UserStore
import vip.mystery0.xhu.timetable.config.store.UserStore.withAutoLoginOnce
import vip.mystery0.xhu.timetable.model.ws.AdminStatus
import vip.mystery0.xhu.timetable.model.ws.ClientMessageContentType
import vip.mystery0.xhu.timetable.model.ws.ClientMessageWrapper
import vip.mystery0.xhu.timetable.model.ws.UnknownSystemMessage
import vip.mystery0.xhu.timetable.model.ws.WsMessageType
import vip.mystery0.xhu.timetable.model.ws.parseMessage
import vip.mystery0.xhu.timetable.platform.ArkTsBridge
import vip.mystery0.xhu.timetable.repository.UserRepo
import vip.mystery0.xhu.timetable.utils.parseJsonToObj

private var validatedFeedbackToken = ""
private var feedbackGeneration = 0
private var feedbackAdminOnline = false
private val feedbackStatusMutex = Mutex()

fun invalidateNativeFeedback() {
    feedbackGeneration++
    validatedFeedbackToken = ""
    feedbackAdminOnline = false
}

suspend fun closeNativeFeedback() {
    invalidateNativeFeedback()
    ArkTsBridge.call("closeFeedback", "")
}

suspend fun nativeFeedbackStatus(): JsonObject = feedbackStatusMutex.withLock {
    val generation = feedbackGeneration
    val snapshot = Json.parseToJsonElement(ArkTsBridge.call("feedbackSnapshot", "")).jsonObject
    // 退出期间已经在途的读取不能重新写回旧会话的管理员状态。
    if (generation != feedbackGeneration) {
        return@withLock buildJsonObject {
            put("state", "DISCONNECTED")
            put("message", "反馈连接已关闭")
            put("adminOnline", false)
        }
    }
    val state = snapshot["state"]?.jsonPrimitive?.content ?: "DISCONNECTED"
    if (state == "CONNECTED") {
        for (event in snapshot["events"]?.jsonArray.orEmpty()) {
            try {
                val text = (event as? JsonPrimitive)?.takeIf { it.isString }?.content ?: continue
                val wrapper = text.parseJsonToObj<ClientMessageWrapper>()
                if (wrapper.contentType != ClientMessageContentType.SYSTEM) continue
                // 共享解析器会记录未知系统消息；先用共享模型筛选，避免正文进入日志。
                val systemMessage = wrapper.content.parseJsonToObj<UnknownSystemMessage>()
                if (systemMessage.type != WsMessageType.ADMIN_STATUS.type) continue
                val adminStatus = parseMessage(text) as? AdminStatus ?: continue
                feedbackAdminOnline = adminStatus.online
            } catch (error: CancellationException) {
                throw error
            } catch (_: Exception) {
                // 单条非法系统消息不影响后续事件，也不保留或记录消息正文。
            }
        }
    } else {
        feedbackAdminOnline = false
    }
    buildJsonObject {
        put("state", state)
        put("message", snapshot["message"]?.jsonPrimitive?.content.orEmpty())
        put("adminOnline", feedbackAdminOnline)
    }
}

suspend fun connectNativeFeedback() {
    val generation = feedbackGeneration
    var user = UserStore.mainUser()
    if (validatedFeedbackToken != user.token) {
        user.withAutoLoginOnce { UserRepo.getUserInfo(it) }
        user = UserStore.mainUser()
        check(generation == feedbackGeneration) { "反馈页面已关闭" }
        validatedFeedbackToken = user.token
        feedbackAdminOnline = false
    }
    check(generation == feedbackGeneration) { "反馈页面已关闭" }
    val url = URLBuilder("wss://ws.api.mystery0.vip/ws").apply {
        parameters.append("sessionToken", user.token)
        parameters.append("wrap", "true")
    }.buildString()
    val request = buildJsonObject {
        put("url", url)
        put("headers", JsonArray(listOf(
            "deviceId" to publicDeviceId(), "clientVersionName" to appVersionName(), "clientVersionCode" to appVersionCode(),
        ).map { (name, value) -> buildJsonObject { put("name", name); put("value", value) } }))
    }
    ArkTsBridge.call("connectFeedback", request.toString())
}

actual suspend fun readNativeLicenses(): String = ArkTsBridge.call("readLicenses", "")

actual suspend fun sendNativeFeedback(content: String) {
    require(content.isNotBlank()) { "请输入反馈内容" }
    connectNativeFeedback()
    ArkTsBridge.call("sendFeedback", content)
}
