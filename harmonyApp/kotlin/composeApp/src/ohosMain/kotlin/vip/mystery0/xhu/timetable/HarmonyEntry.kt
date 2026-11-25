package vip.mystery0.xhu.timetable

import io.github.vinceglb.filekit.absolutePath
import io.github.vinceglb.filekit.dialogs.initializeFileSharing
import io.github.vinceglb.filekit.initializeFileKit
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.initMainHandler
import kotlinx.serialization.json.Json
import org.koin.core.context.startKoin
import platform.ArkTS.ArkTS_Napi_NativeModule.napi_env
import platform.ArkTS.ArkTS_Napi_NativeModule.napi_throw_error
import platform.ArkTS.ArkTS_Napi_NativeModule.napi_value
import vip.mystery0.xhu.timetable.module.moduleList
import vip.mystery0.xhu.timetable.nativeui.NativeCore
import vip.mystery0.xhu.timetable.nativeui.NativeRequest
import vip.mystery0.xhu.timetable.platform.ArkTsBridge
import vip.mystery0.xhu.timetable.platform.OhosPlatform
import vip.mystery0.xhu.timetable.platform.readArkTsString
import kotlin.experimental.ExperimentalNativeApi

private var applicationInitialized = false

@OptIn(ExperimentalNativeApi::class, ExperimentalForeignApi::class)
@CName("HarmonyInitialize")
fun initializeHarmony(env: napi_env, filesDir: napi_value, cacheDir: napi_value, appInfo: napi_value, services: napi_value) {
    try {
        initMainHandler(env)
        initializeFileKit(readArkTsString(env, filesDir), readArkTsString(env, cacheDir))
        ArkTsBridge.initialize(env, services)
        OhosPlatform.initialize(readArkTsString(env, appInfo))
        initializeFileSharing { files ->
            ArkTsBridge.call("shareFiles", Json.encodeToString(files.map { it.absolutePath() }))
        }
        if (!applicationInitialized) {
            initLogger()
            initCoroutine()
            startKoin { modules(moduleList()) }
            applicationInitialized = true
        }
        NativeCore.start()
    } catch (_: Throwable) {
        napi_throw_error(env, "INIT_FAILED", "应用初始化失败，请重新打开")
    }
}

@OptIn(ExperimentalNativeApi::class, ExperimentalForeignApi::class)
@CName("HarmonyDispatch")
fun dispatchHarmony(env: napi_env, value: napi_value) {
    try {
        NativeCore.dispatch(NativeRequest.parse(readArkTsString(env, value)))
    } catch (_: Throwable) {
        napi_throw_error(env, "INVALID_REQUEST", "应用请求无效")
    }
}

@OptIn(ExperimentalNativeApi::class)
@CName("HarmonyDispose")
fun disposeHarmony() = NativeCore.dispose()

@OptIn(ExperimentalNativeApi::class)
@CName("UpdateNetworkState")
fun updateNetworkState(connected: Boolean) {
    OhosPlatform.networkConnected = connected
}
