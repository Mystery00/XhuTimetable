package org.slf4j.impl

import org.slf4j.ILoggerFactory
import org.slf4j.Logger

object MyLoggerFactory : ILoggerFactory {
    const val TRACE_ENABLE = false
    const val DEBUG_ENABLE = false
    const val INFO_ENABLE = false
    const val WARN_ENABLE = true
    const val ERROR_ENABLE = true

    override fun getLogger(name: String): Logger = Logger(name)
}