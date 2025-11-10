package vip.mystery0.xhu.timetable

import kotlin.system.exitProcess

actual fun killCurrentProcess() {
    android.os.Process.killProcess(android.os.Process.myPid())
    exitProcess(10)
}