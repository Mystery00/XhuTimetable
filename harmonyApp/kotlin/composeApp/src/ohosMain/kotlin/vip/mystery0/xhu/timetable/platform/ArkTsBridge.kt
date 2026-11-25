@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package vip.mystery0.xhu.timetable.platform

import kotlinx.cinterop.*
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import platform.ArkTS.ArkTS_Napi_NativeModule.*
import platform.posix.size_tVar
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

object ArkTsBridge {
    private var environment: napi_env? = null
    private var services: napi_ref? = null

    fun initialize(env: napi_env, value: napi_value) = memScoped {
        services?.let { napi_delete_reference(environment, it) }
        environment = env
        val reference = alloc<napi_refVar>()
        checkStatus(napi_create_reference(env, value, 1u, reference.ptr))
        services = reference.value
    }

    suspend fun call(method: String, payload: String): String = withContext(Dispatchers.Main.immediate) {
        suspendCancellableCoroutine { continuation ->
            val pending = StableRef.create(continuation)
            try {
                memScoped {
                    val target = alloc<napi_valueVar>()
                    checkStatus(napi_get_reference_value(environment, services, target.ptr))
                    val function = alloc<napi_valueVar>()
                    checkStatus(napi_get_named_property(environment, target.value, method, function.ptr))
                    val argument = alloc<napi_valueVar>()
                    checkStatus(napi_create_string_utf8(environment, payload, NAPI_AUTO_LENGTH, argument.ptr))
                    val promise = alloc<napi_valueVar>()
                    checkStatus(napi_call_function(environment, target.value, function.value, 1.convert(), argument.ptr, promise.ptr))
                    val then = alloc<napi_valueVar>()
                    checkStatus(napi_get_named_property(environment, promise.value, "then", then.ptr))
                    val callbacks = allocArray<napi_valueVar>(2)
                    checkStatus(napi_create_function(environment, "resolve", NAPI_AUTO_LENGTH, staticCFunction(::resolveCall), pending.asCPointer(), callbacks))
                    checkStatus(napi_create_function(environment, "reject", NAPI_AUTO_LENGTH, staticCFunction(::rejectCall), pending.asCPointer(), callbacks + 1))
                    checkStatus(napi_call_function(environment, promise.value, then.value, 2.convert(), callbacks, null))
                }
            } catch (error: Throwable) {
                pending.dispose()
                continuation.resumeWithException(error)
            }
        }
    }
}

fun readArkTsString(env: napi_env?, value: napi_value?): String = memScoped {
    val length = alloc<size_tVar>()
    checkStatus(napi_get_value_string_utf8(env, value, null, 0.convert(), length.ptr))
    val buffer = allocArray<ByteVar>(length.value.toInt() + 1)
    checkStatus(napi_get_value_string_utf8(env, value, buffer, (length.value + 1u).convert(), length.ptr))
    buffer.readBytes(length.value.toInt()).decodeToString()
}

private fun checkStatus(status: napi_status) {
    check(status == napi_ok) { "鸿蒙平台调用失败：$status" }
}

private fun resolveCall(env: napi_env?, info: napi_callback_info?): napi_value? =
    completeCall(env, info, false)

private fun rejectCall(env: napi_env?, info: napi_callback_info?): napi_value? =
    completeCall(env, info, true)

private fun completeCall(env: napi_env?, info: napi_callback_info?, failed: Boolean): napi_value? = memScoped {
    val count = alloc<size_tVar> { value = 1.convert() }
    val argument = alloc<napi_valueVar>()
    val data = alloc<COpaquePointerVar>()
    checkStatus(napi_get_cb_info(env, info, count.ptr, argument.ptr, null, data.ptr))
    val pending = data.value!!.asStableRef<CancellableContinuation<String>>()
    val continuation = pending.get()
    try {
        if (continuation.isActive) {
            val text = alloc<napi_valueVar>()
            checkStatus(napi_coerce_to_string(env, argument.value, text.ptr))
            val message = readArkTsString(env, text.value)
            if (failed) continuation.resumeWithException(IllegalStateException(message))
            else continuation.resume(message)
        }
    } catch (error: Throwable) {
        if (continuation.isActive) continuation.resumeWithException(error)
    } finally {
        pending.dispose()
    }
    val result = alloc<napi_valueVar>()
    napi_get_undefined(env, result.ptr)
    result.value
}
