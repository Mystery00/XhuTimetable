package vip.mystery0.xhu.timetable.config.store

import com.tencent.mmkv.MMKV

object PoemsStore {
    private val kv = MMKV.mmkvWithID("PoemsStore", MMKV.SINGLE_PROCESS_MODE)

    private const val disablePoemsKey = "disablePoems"
    var disablePoems: Boolean
        set(value) {
            kv.encode(disablePoemsKey, value)
        }
        get() = kv.decodeBool(disablePoemsKey, false)
    private const val tokenKey = "token"
    var token: String?
        get() = kv.decodeString(tokenKey)
        set(value) {
            if (value == null) {
                kv.removeValueForKey(tokenKey)
                return
            }
            kv.encode(tokenKey, value)
        }
    private const val showPoemsTranslateKey = "showPoemsTranslate"
    var showPoemsTranslate: Boolean
        set(value) {
            kv.encode(showPoemsTranslateKey, value)
        }
        get() = kv.decodeBool(showPoemsTranslateKey, true)
}