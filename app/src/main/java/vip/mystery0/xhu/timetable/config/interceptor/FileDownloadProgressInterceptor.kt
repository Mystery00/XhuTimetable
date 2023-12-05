package vip.mystery0.xhu.timetable.config.interceptor

import okhttp3.Interceptor
import okhttp3.MediaType
import okhttp3.Response
import okhttp3.ResponseBody
import okio.Buffer
import okio.BufferedSource
import okio.ForwardingSource
import okio.Source
import okio.buffer

class FileDownloadProgressInterceptor(
    private val progressUpdater: (FileDownloadProgressState) -> Unit,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val originalResponse = chain.proceed(request)

        return originalResponse.newBuilder()
            .body(FileDownloadProgressResponseBody(originalResponse.body!!, progressUpdater))
            .build()
    }
}

open class FileDownloadProgressResponseBody(
    private val responseBody: ResponseBody,
    private val progressUpdater: (FileDownloadProgressState) -> Unit,
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
                val progress = totalBytesRead * 100 / totalSize.toFloat()
                progressUpdater(FileDownloadProgressState(totalBytesRead, totalSize, progress))
                return bytesRead
            }
        }
    }
}

data class FileDownloadProgressState(
    val received: Long = 1L,
    val total: Long = 1L,
    val progress: Float = 100.0F,
)