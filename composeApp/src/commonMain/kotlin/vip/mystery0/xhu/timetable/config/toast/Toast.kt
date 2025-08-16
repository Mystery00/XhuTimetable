package vip.mystery0.xhu.timetable.config.toast

import co.touchlab.kermit.Logger
import multiplatform.network.cmptoast.ToastDuration
import multiplatform.network.cmptoast.showToast

fun showLongToast(message: String) {
    runCatching {
        showToast(message = message, duration = ToastDuration.Long)
    }.onFailure {
        Logger.w("showToast failed", it)
    }
}

fun showShortToast(message: String) {
    runCatching {
        showToast(message = message, duration = ToastDuration.Short)
    }.onFailure {
        Logger.w("showToast failed", it)
    }
}