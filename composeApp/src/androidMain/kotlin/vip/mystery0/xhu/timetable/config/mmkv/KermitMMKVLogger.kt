package vip.mystery0.xhu.timetable.config.mmkv

import co.touchlab.kermit.Logger
import com.tencent.mmkv.MMKVHandler
import com.tencent.mmkv.MMKVLogLevel
import com.tencent.mmkv.MMKVRecoverStrategic

class KermitMMKVLogger : MMKVHandler {
    override fun onMMKVCRCCheckFail(mmapID: String): MMKVRecoverStrategic =
        MMKVRecoverStrategic.OnErrorDiscard

    override fun onMMKVFileLengthError(mmapID: String): MMKVRecoverStrategic =
        MMKVRecoverStrategic.OnErrorDiscard

    override fun wantLogRedirecting(): Boolean = true

    override fun mmkvLog(
        level: MMKVLogLevel,
        file: String,
        line: Int,
        func: String,
        message: String,
    ) {
        val logger = Logger.withTag("$file:$line")
        val log = "<$func> $message"
        when (level) {
            MMKVLogLevel.LevelDebug -> logger.d(log)
            MMKVLogLevel.LevelInfo -> logger.i(log)
            MMKVLogLevel.LevelWarning -> logger.w(log)
            MMKVLogLevel.LevelError -> logger.e(log)
            MMKVLogLevel.LevelNone -> {}
        }
    }
}