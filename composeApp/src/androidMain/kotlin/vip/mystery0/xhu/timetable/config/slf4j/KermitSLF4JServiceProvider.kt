package vip.mystery0.xhu.timetable.config.slf4j

import com.google.auto.service.AutoService
import org.slf4j.ILoggerFactory
import org.slf4j.IMarkerFactory
import org.slf4j.helpers.BasicMarkerFactory
import org.slf4j.helpers.NOPMDCAdapter
import org.slf4j.spi.MDCAdapter
import org.slf4j.spi.SLF4JServiceProvider

@AutoService(SLF4JServiceProvider::class)
class KermitSLF4JServiceProvider : SLF4JServiceProvider {
    companion object {
        @JvmStatic
        private val markerFactory1 = BasicMarkerFactory()
        @JvmStatic
        private val mdcAdapter1 = NOPMDCAdapter()
    }

    override fun getLoggerFactory(): ILoggerFactory = MyLoggerFactory

    override fun getMarkerFactory(): IMarkerFactory = markerFactory1

    override fun getMDCAdapter(): MDCAdapter = mdcAdapter1

    override fun getRequestedApiVersion(): String = "2.0"

    override fun initialize() {
    }
}