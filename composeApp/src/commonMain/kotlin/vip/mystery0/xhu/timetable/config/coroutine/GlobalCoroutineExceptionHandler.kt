package vip.mystery0.xhu.timetable.config.coroutine

import co.touchlab.kermit.Logger
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.cancellation.CancellationException

object GlobalCoroutineExceptionHandler : CoroutineExceptionHandler {
    override val key: CoroutineContext.Key<*> = CoroutineExceptionHandler

    // 处理器现在接收 CoroutineContext，而不是 Java 的 Thread 对象
    private var globalHandler: ((CoroutineContext, Throwable) -> Unit)? = null

    /**
     * 设置全局的、未捕获异常的处理器。
     * @param handler 当有未捕获的异常发生时将被调用的 lambda，它接收协程上下文和异常。
     */
    fun setHandler(handler: (context: CoroutineContext, throwable: Throwable) -> Unit) {
        globalHandler = handler
    }

    override fun handleException(context: CoroutineContext, exception: Throwable) {
        // Coroutine 的 CancellationException 是正常取消流程的一部分，通常不需要作为错误处理。
        if (exception is CancellationException) {
            return
        }

        Logger.e("[Global] got exception: ${exception.message}")

        // 如果用户设置了自定义的全局处理器，则调用它。
        // 否则，将堆栈跟踪打印到标准输出（替代了 System.err）。
        globalHandler?.invoke(context, exception) ?: {
            Logger.e("[Global] global handler not set, rethrow exception.")
            throw exception
        }
    }
}