package vip.mystery0.xhu.timetable.config.logger

import co.touchlab.kermit.Severity
import kotlinx.coroutines.channels.Channel
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DisabledFileLogWriterTest {
    @Test
    fun disabledWriterRejectsMessagesBeforeFormatting() {
        val writer = FileLogWriter()
        try {
            assertFalse(writer.isLoggable("test", Severity.Info))
        } finally {
            writer.stop()
        }
    }

    @Test
    fun disabledWriterDoesNotRetainDirectLogCalls() {
        val writer = FileLogWriter()
        try {
            repeat(100) { writer.log(Severity.Info, "测试日志-$it", "test", null) }
            val field = FileLogWriter::class.java.getDeclaredField("logChannel").apply {
                isAccessible = true
            }
            val channel = field.get(writer) as Channel<*>
            assertTrue(channel.tryReceive().isFailure)
        } finally {
            writer.stop()
        }
    }
}
