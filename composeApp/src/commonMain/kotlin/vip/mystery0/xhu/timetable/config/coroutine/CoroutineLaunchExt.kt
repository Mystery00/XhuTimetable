package vip.mystery0.xhu.timetable.config.coroutine

import co.touchlab.kermit.Logger
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.cancellation.CancellationException

fun CoroutineScope.safeLaunch(
    context: CoroutineContext = Dispatchers.Default,
    onException: ((throwable: Throwable) -> Boolean)? = null,
    block: suspend CoroutineScope.() -> Unit
): Job {
    // 为本次 launch 创建一个专属的 CoroutineExceptionHandler
    val localExceptionHandler = CoroutineExceptionHandler { handlerContext, exception ->
        // 同样，忽略正常的取消异常
        if (exception is CancellationException) return@CoroutineExceptionHandler

        Logger.d("[safeLaunch] got exception: ${exception.message}")

        // 调用用户提供的局部异常处理器，如果为 null，则默认未消费 (isConsumed = false)
        val isConsumed = onException?.invoke(exception) ?: false

        // 如果局部处理器没有消费该异常，则手动委托给全局处理器
        if (!isConsumed) {
            Logger.d("[safeLaunch] exception not consumed, delegate to global handler.")
            GlobalCoroutineExceptionHandler.handleException(handlerContext, exception)
        } else {
            Logger.d("[safeLaunch] exception consumed by local handler.")
        }
    }

    // 启动协程，合并传入的上下文、默认后台调度器以及我们自定义的局部异常处理器。
    return this.launch(context + localExceptionHandler) {
        block()
    }
}

suspend fun <T> safeWithContext(
    context: CoroutineContext,
    onException: ((throwable: Throwable) -> Boolean)? = null,
    resultWhenException: suspend () -> T,
    block: suspend CoroutineScope.() -> T,
): T {
    return try {
        withContext(context) {
            block()
        }
    } catch (e: Throwable) {
        if (e is CancellationException) {
            throw e
        }
        Logger.d("[safeWithContext] got exception: ${e.message}")
        val isConsumed = onException?.invoke(e) ?: false
        if (isConsumed) {
            Logger.d("[safeWithContext] exception consumed by onException handler.")
            resultWhenException()
        } else {
            Logger.d("[safeWithContext] exception not consumed, delegate to parent handler.")
            throw e // 未消费，重新抛出给上层
        }
    }
}