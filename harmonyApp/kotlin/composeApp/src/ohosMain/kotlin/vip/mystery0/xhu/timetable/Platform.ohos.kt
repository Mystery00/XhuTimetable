package vip.mystery0.xhu.timetable

import co.touchlab.kermit.Logger
import vip.mystery0.xhu.timetable.platform.OhosPlatform

actual val isDebug: Boolean = false
actual val enableUpdateCheck: Boolean = false
actual fun platform(): Platform = Platform.HARMONY
actual fun killCurrentProcess() = OhosPlatform.launch("terminate")
actual fun handleGlobalException(throwable: Throwable) {
    Logger.e(throwable) { "鸿蒙应用未处理异常" }
}
