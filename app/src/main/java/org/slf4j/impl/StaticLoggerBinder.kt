package org.slf4j.impl

import org.slf4j.ILoggerFactory
import org.slf4j.spi.LoggerFactoryBinder

class StaticLoggerBinder : LoggerFactoryBinder {
    override fun getLoggerFactory(): ILoggerFactory = MyLoggerFactory

    override fun getLoggerFactoryClassStr(): String = MyLoggerFactory::class.java.name

    companion object {
        private val instance = StaticLoggerBinder()

        @JvmStatic
        fun getSingleton(): StaticLoggerBinder = instance
    }
}