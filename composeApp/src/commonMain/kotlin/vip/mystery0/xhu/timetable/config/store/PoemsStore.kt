package vip.mystery0.xhu.timetable.config.store

import co.touchlab.kermit.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import vip.mystery0.xhu.timetable.api.PoemsApi
import vip.mystery0.xhu.timetable.config.Feature
import vip.mystery0.xhu.timetable.model.response.Poems
import vip.mystery0.xhu.timetable.utils.isOnline

object PoemsStore : KoinComponent {
    private val logger = Logger.withTag(tag = "PoemsStore")
    private val poemsApi: PoemsApi by inject()

    private const val disablePoemsKey = "disablePoems"
    var disablePoems: Boolean
        set(value) {
            Store.PoemsStore.setConfiguration(disablePoemsKey, value)
        }
        get() = Store.PoemsStore.getConfiguration(disablePoemsKey, false)

    private const val tokenKey = "token"
    var token: String?
        get() = Store.PoemsStore.getConfiguration(tokenKey, "").ifBlank { null }
        set(value) {
            if (value == null) {
                Store.PoemsStore.removeConfiguration(tokenKey)
                return
            }
            Store.PoemsStore.setConfiguration(tokenKey, value)
        }

    private const val showPoemsTranslateKey = "showPoemsTranslate"
    var showPoemsTranslate: Boolean
        set(value) {
            Store.PoemsStore.setConfiguration(showPoemsTranslateKey, value)
        }
        get() = Store.PoemsStore.getConfiguration(showPoemsTranslateKey, true)

    suspend fun loadPoems(): Poems? {
        if (disablePoems) {
            return null
        }
        if (!Feature.JRSC.isEnabled()) {
            logger.i( "disable jinrishici with feature switch")
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