package vip.mystery0.xhu.timetable.config

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

suspend fun <T> runOnCpu(block: suspend CoroutineScope.() -> T): T =
    withContext(Dispatchers.Default, block)