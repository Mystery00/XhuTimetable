package vip.mystery0.xhu.timetable.config.store

import com.tencent.mmkv.MMKV
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import vip.mystery0.xhu.timetable.api.PoemsApi
import vip.mystery0.xhu.timetable.isOnline
import vip.mystery0.xhu.timetable.model.response.Poems
import vip.mystery0.xhu.timetable.module.Feature

object PoemsStore : KoinComponent {
    private val poemsApi: PoemsApi by inject()
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

    suspend fun loadPoems(): Poems? {
        if (disablePoems) {
            return null
        }
        if (!Feature.JRSC.isEnabled()) {
            return null
        }
        if (!isOnline()) {
            return null
        }
        if (token.isNullOrBlank()) {
            token = withContext(Dispatchers.IO) { poemsApi.getToken() }.data
        }
        val poems = withContext(Dispatchers.IO) { poemsApi.getSentence() }.data
        if (!showPoemsTranslate) {
            poems.origin.translate = null
        }
        return poems
    }
}