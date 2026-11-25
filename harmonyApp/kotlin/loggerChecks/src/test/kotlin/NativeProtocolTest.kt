import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import vip.mystery0.xhu.timetable.nativeui.NativeRequest

class NativeProtocolTest {
    @Test
    fun parsesRequestWithoutLosingUnicodeOrEscapes() {
        val request = NativeRequest.parse("""{"id":"7","action":"login","payload":{"password":"密\\码\""}}""")
        assertEquals("7", request.id)
        assertEquals("login", request.action)
        assertEquals("密\\码\"", request.payload["password"]?.toString()?.let {
            kotlinx.serialization.json.Json.decodeFromString<String>(it)
        })
    }

    @Test
    fun missingPayloadIsAnEmptyObject() {
        assertEquals(0, NativeRequest.parse("""{"id":"1","action":"bootstrap"}""").payload.size)
    }

    @Test
    fun rejectsRequestsThatCannotBeCorrelated() {
        assertFailsWith<IllegalArgumentException> {
            NativeRequest.parse("""{"id":"","action":"bootstrap","payload":{}}""")
        }
    }

    @Test
    fun malformedPayloadDoesNotExposeInputInError() {
        val error = assertFailsWith<IllegalArgumentException> {
            NativeRequest.parse("""{"id":"1","action":"login","payload":["secret-password"]}""")
        }
        assertFalse(error.message.orEmpty().contains("secret-password"))
    }
}
