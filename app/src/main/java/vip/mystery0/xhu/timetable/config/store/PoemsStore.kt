package vip.mystery0.xhu.timetable.config.store

import com.tencent.mmkv.MMKV

object PoemsStore {
    private val kv = MMKV.mmkvWithID("PoemsStore", MMKV.SINGLE_PROCESS_MODE)

    var token: String?
        get() = kv.decodeString("token")
        set(value) {
            if (value == null) {
                kv.removeValueForKey("token")
                return
            }
            kv.encode("token", value)
        }
}