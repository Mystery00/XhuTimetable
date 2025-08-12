package vip.mystery0.xhu.timetable.config.logger

import co.touchlab.kermit.LogWriter
import co.touchlab.kermit.Severity
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.format
import kotlinx.datetime.format.char
import vip.mystery0.xhu.timetable.utils.asLocalDateTime
import kotlin.time.Clock

/**
 * A custom Kermit LogWriter that buffers logs in memory and writes them to a file
 * asynchronously in batches.
 *
 * This approach prevents file I/O operations from blocking the thread that calls the logger.
 *
 * @param context A platform-specific context object. Required for Android (pass `android.content.Context`),
 * but can be ignored for other platforms.
 * @param batchSize The number of log messages to buffer before forcing a write to the file.
 * @param maxDelayMs The maximum time to wait before writing the buffered logs, even if
 * the batch size has not been reached.
 */
class FileLogWriter(
    private val batchSize: Int = 100,
    private val maxDelayMs: Long = 5000L
) : LogWriter() {
    private val logChannel = Channel<String>(Channel.UNLIMITED)
    private val fileHandler = LogFileHandler()

    private val coroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var processingJob: Job? = null

    private val dateFormatter = LocalDateTime.Format {
        year()
        char('-')
        monthNumber()
        char('-')
        day()
        char(' ')
        hour()
        char(':')
        minute()
        char(':')
        second()
    }

    init {
        if (fileHandler.enable()) {
            start()
        }
    }

    /**
     * This method is called by Kermit for each log message.
     * It formats the message and sends it to a non-blocking channel for processing.
     */
    override fun log(severity: Severity, message: String, tag: String, throwable: Throwable?) {
        val timestamp = Clock.System.now().asLocalDateTime().format(dateFormatter)
        val formattedMessage = "$timestamp ${severity.name.padEnd(7)} [$tag]: $message"
        val fullMessage = if (throwable != null) {
            "$formattedMessage\n${throwable.stackTraceToString()}"
        } else {
            formattedMessage
        }
        // trySend is non-blocking. If the channel were to get full (unlikely with UNLIMITED),
        // it would drop the log instead of blocking, preserving app performance.
        logChannel.trySend(fullMessage)
    }

    /**
     * Starts the background coroutine that processes the log channel.
     */
    private fun start() {
        processingJob = coroutineScope.launch {
            val batch = mutableListOf<String>()
            var lastWriteTime = Clock.System.now().toEpochMilliseconds()

            while (isActive) {
                try {
                    // Wait for a log to arrive, but with a timeout.
                    // This allows the loop to wake up either when a log is available or when the maxDelay is hit.
                    val logMessage = withTimeoutOrNull(maxDelayMs) {
                        logChannel.receive()
                    }

                    val currentTime = Clock.System.now().toEpochMilliseconds()

                    if (logMessage != null) {
                        batch.add(logMessage)
                    }

                    // Determine if logs should be written to the file. This happens if:
                    // 1. The batch has reached its maximum size.
                    // 2. The timeout has been reached since the last write, and there's something in the batch.
                    val shouldWrite =
                        batch.size >= batchSize || (currentTime - lastWriteTime >= maxDelayMs && batch.isNotEmpty())

                    if (shouldWrite) {
                        fileHandler.writeLogs(batch.toList()) // Write a copy for thread safety
                        batch.clear()
                        lastWriteTime = currentTime
                    }
                } catch (e: CancellationException) {
                    // Scope is being cancelled. Flush any remaining logs before exiting.
                    println("FileLogWriter scope cancelled. Flushing remaining logs...")
                    if (batch.isNotEmpty()) {
                        fileHandler.writeLogs(batch)
                    }
                    flushRemainingChannelLogs()
                    break // Exit the loop
                } catch (e: Exception) {
                    // In case of other errors, print them but don't crash the logger.
                    println("Error in FileLogWriter processing loop: $e")
                }
            }
        }
    }

    fun stop() {
        coroutineScope.cancel()
    }

    private fun flushRemainingChannelLogs() {
        val remainingLogs = mutableListOf<String>()
        while (true) {
            val log = logChannel.tryReceive().getOrNull() ?: break
            remainingLogs.add(log)
        }
        if (remainingLogs.isNotEmpty()) {
            fileHandler.writeLogs(remainingLogs)
        }
    }
}