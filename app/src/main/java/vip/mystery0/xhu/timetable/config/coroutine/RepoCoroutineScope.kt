package vip.mystery0.xhu.timetable.config.coroutine

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asCoroutineDispatcher
import java.util.concurrent.Executors

object RepoCoroutineScope {
    private val dispatcher = Executors.newFixedThreadPool(5).asCoroutineDispatcher()
    val scope = CoroutineScope(dispatcher)
}