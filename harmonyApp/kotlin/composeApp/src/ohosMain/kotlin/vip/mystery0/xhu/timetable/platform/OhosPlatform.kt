package vip.mystery0.xhu.timetable.platform

import co.touchlab.kermit.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import vip.mystery0.xhu.timetable.config.toast.showShortToast

@Serializable
data class OhosAppInfo(
    val bundleName: String,
    val versionName: String,
    val versionCode: Long,
    val systemVersion: String,
    val manufacturer: String,
    val model: String,
    val networkConnected: Boolean,
)

object OhosPlatform {
    lateinit var info: OhosAppInfo
        private set
    var networkConnected = true
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    fun initialize(json: String) {
        info = Json.decodeFromString<OhosAppInfo>(json)
        networkConnected = info.networkConnected
    }

    fun launch(method: String, payload: String = "") {
        scope.launch {
            try {
                ArkTsBridge.call(method, payload)
            } catch (error: Exception) {
                Logger.w("鸿蒙系统操作失败：$method")
                showShortToast(error.message ?: "系统操作失败，请重试")
            }
        }
    }
}
