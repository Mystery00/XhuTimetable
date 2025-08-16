package vip.mystery0.xhu.timetable

import co.touchlab.kermit.Logger
import kotlinx.coroutines.CoroutineName
import vip.mystery0.xhu.timetable.config.coroutine.GlobalCoroutineExceptionHandler

fun initCoroutine() {
    GlobalCoroutineExceptionHandler.setHandler { context, throwable ->
        val coroutineName = context[CoroutineName]?.name ?: "UnnamedCoroutine"
        Logger.i("[${coroutineName}] global got exception, class: ${throwable::class.simpleName}, message: ${throwable.message}")
        handleGlobalException(throwable)
    }
}

expect fun handleGlobalException(throwable: Throwable)