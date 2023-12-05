package vip.mystery0.xhu.timetable.config.interceptor

import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.ResponseBody
import vip.mystery0.xhu.timetable.ui.activity.DownloadUpdateState
import vip.mystery0.xhu.timetable.ui.activity.updateProgress

/**
 * Created by JokAr-.
 * 原文地址：http://blog.csdn.net/a1018875550/article/details/51832700
 */
class DownloadProgressInterceptor : Interceptor {
    companion object {
        fun buildTag(
            patch: Boolean,
        ): Tag = Tag(showProgress = true, patch = patch)

        fun emptyTag(): Tag = Tag(showProgress = false, patch = false)
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        val tag = request.tag(Tag::class.java) ?: return chain.proceed(request)
        if (!tag.showProgress) return chain.proceed(request)
        val originalResponse = chain.proceed(request)

        return originalResponse.newBuilder()
            .body(DownloadProgressResponseBody(originalResponse.body!!, tag.patch))
            .build()
    }

    data class Tag(
        val showProgress: Boolean,
        val patch: Boolean,
    )
}

class DownloadProgressResponseBody(
    responseBody: ResponseBody,
    private val patch: Boolean,
) : FileDownloadProgressResponseBody(responseBody, { progress ->
    val state = DownloadUpdateState(
        downloading = true,
        downloaded = progress.received,
        totalSize = progress.total,
        patch = patch,
        progress = progress.progress.toInt(),
        status = "${progress.progress.toInt()}%",
    )
    updateProgress(state)
})