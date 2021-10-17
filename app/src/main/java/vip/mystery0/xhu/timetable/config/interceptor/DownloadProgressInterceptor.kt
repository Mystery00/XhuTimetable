package vip.mystery0.xhu.timetable.config.interceptor

import okhttp3.Interceptor
import okhttp3.MediaType
import okhttp3.Response
import okhttp3.ResponseBody
import okio.*
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
        ): Tag = Tag(true, patch)

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
    private val responseBody: ResponseBody,
    private val patch: Boolean,
) : ResponseBody() {
    private val bufferedSource: BufferedSource by lazy { source(responseBody.source()).buffer() }

    override fun contentType(): MediaType? = responseBody.contentType()

    override fun contentLength(): Long = responseBody.contentLength()

    override fun source(): BufferedSource = bufferedSource

    private fun source(source: Source): Source {
        return object : ForwardingSource(source) {
            var totalBytesRead = 0L
            val totalSize = responseBody.contentLength()

            override fun read(sink: Buffer, byteCount: Long): Long {
                val bytesRead = super.read(sink, byteCount)
                totalBytesRead += if (bytesRead != -1L) bytesRead else 0
                val progress = (totalBytesRead * 100 / totalSize).toInt()
                val state = DownloadUpdateState(
                    downloading = true,
                    downloaded = totalBytesRead,
                    totalSize = totalSize,
                    patch = patch,
                    progress = progress,
                    status = "已下载${progress}%",
                )
                updateProgress(state)
                return bytesRead
            }
        }
    }
}