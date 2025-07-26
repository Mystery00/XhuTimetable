package vip.mystery0.xhu.timetable.config.ktor

import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.client.request.header
import io.ktor.http.encodedPath
import vip.mystery0.xhu.timetable.config.store.PoemsStore

val PoemsPlugin = createClientPlugin("PoemsPlugin") {
    onRequest { request, _ ->
        if (request.url.encodedPath.contains("one.json")) {
            val token = PoemsStore.token
            if (token.isNullOrBlank()) {
                throw Exception("请求今日诗词出错")
            }
            request.header("X-User-Token", token)
        }
    }
}