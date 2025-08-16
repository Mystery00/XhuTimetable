package vip.mystery0.xhu.timetable.config.logger

import io.github.vinceglb.filekit.PlatformFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.format
import org.apache.commons.compress.archivers.tar.TarArchiveEntry
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream
import vip.mystery0.xhu.timetable.base.packageName
import vip.mystery0.xhu.timetable.context
import vip.mystery0.xhu.timetable.utils.now
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream

actual class LogFileHandler {
    private val logDir: File = File(context.externalCacheDir, "log").apply {
        if (!exists()) {
            mkdirs()
        }
    }

    private var logFile: File =
        File(logDir, "${packageName()}.log").apply {
            if (!exists()) {
                createNewFile()
            }
        }

    private val dateFormatter = LocalDateTime.Format {
        year()
        monthNumber()
        day()
        hour()
        minute()
        second()
    }

    actual fun enable() = true

    private fun rotateLogFileIfNeeded() {
        if (logFile.exists() && logFile.length() > 10 * 1024 * 1024) {
            val newLogFile =
                File(logDir, "${packageName()}_${LocalDateTime.now().format(dateFormatter)}.log")
            logFile.renameTo(newLogFile)
            logFile = File(logDir, "${packageName()}.log").apply { createNewFile() }
        }
    }

    actual fun writeLogs(logs: List<String>) {
        rotateLogFileIfNeeded()
        try {
            logFile.appendText(logs.joinToString(separator = "\n") + "\n")
        } catch (e: Exception) {
            // Log to logcat if file writing fails
            android.util.Log.e("FileLogWriter", "Error writing logs to file on Android", e)
        }
    }

    actual fun clearOld() {
        val files = logDir.listFiles()!!
        if (files.size > 5) {
            files.sortedBy { it.lastModified() }.take(files.size - 5).forEach { it.delete() }
        }
    }

    actual suspend fun prepareSend(): PlatformFile {
        val files = logDir.listFiles()!!
        if (files.size == 0) throw RuntimeException("日志目录为空，操作已取消")

        val now = LocalDateTime.now()
        val time = now.format(LocalDateTime.Format {
            year()
            monthNumber()
            day()
            hour()
            minute()
            second()
        })
        val tempFile = withContext(Dispatchers.IO) {
            val dir = File(context.externalCacheDir, "crash").apply {
                if (!exists()) {
                    mkdirs()
                }
            }
            val tempFile = File(dir, "log-$time.tar.gz")
            val fos = FileOutputStream(tempFile)
            val bos = BufferedOutputStream(fos)
            val gcos = GzipCompressorOutputStream(bos)
            val taos = TarArchiveOutputStream(gcos)

            files.forEach {
                val entry = TarArchiveEntry(it, it.name)
                taos.putArchiveEntry(entry)
                it.inputStream().use { inputStream ->
                    inputStream.copyTo(taos)
                }
                taos.closeArchiveEntry()
            }
            taos.close()
            gcos.close()
            bos.close()
            fos.close()
            tempFile
        }
        return PlatformFile(tempFile)
    }
}