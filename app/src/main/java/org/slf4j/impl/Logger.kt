package org.slf4j.impl

import android.util.Log
import org.slf4j.Marker

class Logger(private val name: String) : org.slf4j.Logger {
    private fun formatMessage(format: String, vararg arguments: Any): String {
        var output = format
        for (i in arguments.indices) {
            output = output.replace("{}", arguments[i].toString(), true)
        }
        return output
    }

    override fun getName(): String = name

    override fun isTraceEnabled(): Boolean = MyLoggerFactory.TRACE_ENABLE

    override fun isTraceEnabled(marker: Marker): Boolean = MyLoggerFactory.TRACE_ENABLE

    override fun trace(msg: String) {}

    override fun trace(format: String, arg: Any) = trace(formatMessage(format, arg))

    override fun trace(format: String, arg1: Any, arg2: Any) =
        trace(formatMessage(format, arg1, arg2))

    override fun trace(format: String, vararg arguments: Any) =
        trace(formatMessage(format, arguments))

    override fun trace(msg: String, t: Throwable) {}

    override fun trace(marker: Marker, msg: String) = trace(msg)

    override fun trace(marker: Marker, format: String, arg: Any) =
        trace(marker, formatMessage(format, arg))

    override fun trace(marker: Marker, format: String, arg1: Any, arg2: Any) =
        trace(marker, formatMessage(format, arg1, arg2))

    override fun trace(marker: Marker, format: String, vararg argArray: Any) =
        trace(marker, formatMessage(format, argArray))

    override fun trace(marker: Marker, msg: String, t: Throwable) = trace(msg, t)

    override fun isDebugEnabled(): Boolean = MyLoggerFactory.DEBUG_ENABLE

    override fun isDebugEnabled(marker: Marker): Boolean = MyLoggerFactory.DEBUG_ENABLE

    override fun debug(msg: String) {
        if (!isDebugEnabled) return
        Log.d(name, msg)
    }

    override fun debug(format: String, arg: Any) = debug(formatMessage(format, arg))

    override fun debug(format: String, arg1: Any, arg2: Any) =
        debug(formatMessage(format, arg1, arg2))

    override fun debug(format: String, vararg arguments: Any) =
        debug(formatMessage(format, arguments))

    override fun debug(msg: String, t: Throwable) {
        if (!isDebugEnabled) return
        Log.d(name, msg, t)
    }

    override fun debug(marker: Marker, msg: String) {
        debug(msg)
    }

    override fun debug(marker: Marker, format: String, arg: Any) =
        debug(marker, formatMessage(format, arg))

    override fun debug(marker: Marker, format: String, arg1: Any, arg2: Any) =
        debug(marker, formatMessage(format, arg1, arg2))

    override fun debug(marker: Marker, format: String, vararg arguments: Any) =
        debug(marker, formatMessage(format, arguments))

    override fun debug(marker: Marker, msg: String, t: Throwable) = debug(msg, t)

    override fun isInfoEnabled(): Boolean = MyLoggerFactory.INFO_ENABLE

    override fun isInfoEnabled(marker: Marker): Boolean = MyLoggerFactory.INFO_ENABLE

    override fun info(msg: String) {
        if (!isInfoEnabled) return
        Log.i(name, msg)
    }

    override fun info(format: String, arg: Any) = info(formatMessage(format, arg))

    override fun info(format: String, arg1: Any, arg2: Any) =
        info(formatMessage(format, arg1, arg2))

    override fun info(format: String, vararg arguments: Any) =
        info(formatMessage(format, arguments))

    override fun info(msg: String, t: Throwable) {
        if (!isInfoEnabled) return
        Log.i(name, msg, t)
    }

    override fun info(marker: Marker, msg: String) {
        info(msg)
    }

    override fun info(marker: Marker, format: String, arg: Any) =
        info(marker, formatMessage(format, arg))

    override fun info(marker: Marker, format: String, arg1: Any, arg2: Any) =
        info(marker, formatMessage(format, arg1, arg2))

    override fun info(marker: Marker, format: String, vararg arguments: Any) =
        info(marker, formatMessage(format, arguments))

    override fun info(marker: Marker, msg: String, t: Throwable) = info(msg, t)

    override fun isWarnEnabled(): Boolean = MyLoggerFactory.WARN_ENABLE

    override fun isWarnEnabled(marker: Marker): Boolean = MyLoggerFactory.WARN_ENABLE

    override fun warn(msg: String) {
        if (!isWarnEnabled) return
        Log.w(name, msg)
    }

    override fun warn(format: String, arg: Any) = warn(formatMessage(format, arg))

    override fun warn(format: String, arg1: Any, arg2: Any) =
        warn(formatMessage(format, arg1, arg2))

    override fun warn(format: String, vararg arguments: Any) =
        warn(formatMessage(format, arguments))

    override fun warn(msg: String, t: Throwable) {
        if (!isWarnEnabled) return
        Log.w(name, msg, t)
    }

    override fun warn(marker: Marker, msg: String) {
        warn(msg)
    }

    override fun warn(marker: Marker, format: String, arg: Any) =
        warn(marker, formatMessage(format, arg))

    override fun warn(marker: Marker, format: String, arg1: Any, arg2: Any) =
        warn(marker, formatMessage(format, arg1, arg2))

    override fun warn(marker: Marker, format: String, vararg arguments: Any) =
        warn(marker, formatMessage(format, arguments))

    override fun warn(marker: Marker, msg: String, t: Throwable) = warn(msg, t)

    override fun isErrorEnabled(): Boolean = MyLoggerFactory.ERROR_ENABLE

    override fun isErrorEnabled(marker: Marker): Boolean = MyLoggerFactory.ERROR_ENABLE

    override fun error(msg: String) {
        if (!isErrorEnabled) return
        Log.e(name, msg)
    }

    override fun error(format: String, arg: Any) = error(formatMessage(format, arg))

    override fun error(format: String, arg1: Any, arg2: Any) =
        error(formatMessage(format, arg1, arg2))

    override fun error(format: String, vararg arguments: Any) =
        error(formatMessage(format, arguments))

    override fun error(msg: String, t: Throwable) {
        if (!isErrorEnabled) return
        Log.e(name, msg, t)
    }

    override fun error(marker: Marker, msg: String) {
        error(msg)
    }

    override fun error(marker: Marker, format: String, arg: Any) =
        error(marker, formatMessage(format, arg))

    override fun error(marker: Marker, format: String, arg1: Any, arg2: Any) =
        error(marker, formatMessage(format, arg1, arg2))

    override fun error(marker: Marker, format: String, vararg arguments: Any) =
        error(marker, formatMessage(format, arguments))

    override fun error(marker: Marker, msg: String, t: Throwable) = error(msg, t)
}