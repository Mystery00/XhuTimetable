package vip.mystery0.xhu.timetable

import vip.mystery0.xhu.timetable.config.toast.showLongToast
import vip.mystery0.xhu.timetable.module.desc

actual fun handleGlobalException(throwable: Throwable) {
    showLongToast(throwable.desc())
}