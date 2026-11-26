package vip.mystery0.xhu.timetable

import kotlinx.serialization.json.Json
import vip.mystery0.xhu.timetable.config.store.Menu
import vip.mystery0.xhu.timetable.model.response.MenuResponse
import kotlin.test.Test
import kotlin.test.assertEquals

class MenuIconTest {
    private val json = Json { ignoreUnknownKeys = true }
    private val fields = """"key":"SETTINGS","title":"软件设置","sort":1,"group":1,"hint":"","link":"""""

    @Test
    fun serverIconSurvivesMenuCacheRoundTrip() {
        val url = "https://example.org/settings.svg"
        val response = json.decodeFromString<MenuResponse>("{$fields,\"icon\":\"$url\"}")
        val restored = json.decodeFromString<Menu>(json.encodeToString(response.toMenu()))
        assertEquals(url, restored.icon)
    }

    @Test
    fun missingAndNullIconsRemainCompatibleWithOldMenuCache() {
        val old = json.decodeFromString<Menu>("{$fields}")
        for (extra in listOf("", ",\"icon\":null")) {
            assertEquals(old, json.decodeFromString<MenuResponse>("{$fields$extra}").toMenu())
        }
    }
}
