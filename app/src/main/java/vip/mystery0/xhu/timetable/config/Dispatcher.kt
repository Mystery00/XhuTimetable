package vip.mystery0.xhu.timetable.config

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

suspend fun <T> runOnMain(block: suspend CoroutineScope.() -> T): T =
    withContext(Dispatchers.Main, block)

suspend fun <T> runOnCpu(block: suspend CoroutineScope.() -> T): T =
    withContext(Dispatchers.Default, block)

suspend fun <T> runOnIo(block: suspend CoroutineScope.() -> T): T =
    withContext(Dispatchers.IO, block)