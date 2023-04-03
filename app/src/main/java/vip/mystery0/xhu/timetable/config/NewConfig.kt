package vip.mystery0.xhu.timetable.config

import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.tencent.mmkv.MMKV
import vip.mystery0.xhu.timetable.model.response.Splash
import java.time.LocalDate
import java.time.format.DateTimeFormatter

object Formatter {
    val DATE = DateTimeFormatter.ISO_LOCAL_DATE
}

private val instance = NewConfig()
val GlobalNewConfig = instance

suspend fun <T> getNewConfig(block: NewConfig.() -> T) = runOnIo { block(instance) }
suspend fun setNewConfig(block: suspend NewConfig.() -> Unit) = runOnIo { block(instance) }

class NewConfig internal constructor() {
    private val kv = MMKV.mmkvWithID("NewConfig")
    private val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()

    private val termStartDateKey = "termStartDate"
    val termStartDate: LocalDate
        get() = customTermStartDate.data
    var customTermStartDate: Customisable<LocalDate>
        set(value) {
            val key = value.mapKey(termStartDateKey)
            val saveValue = value.data.format(Formatter.DATE)
            kv.encode(key, saveValue)
        }
        get() {
            val customValue: String? = kv.decodeString(Customisable.customKey(termStartDateKey))
            if (!customValue.isNullOrBlank()) {
                return Customisable(LocalDate.parse(customValue, Formatter.DATE), true)
            }
            val value: String? = kv.decodeString(termStartDateKey)
            if (!value.isNullOrBlank()) {
                return Customisable(LocalDate.parse(value, Formatter.DATE), false)
            }
            // 默认值
            return Customisable(LocalDate.of(2023, 2, 20), false)
        }

    private val nowYearKey = "nowYear"
    val nowYear: Int
        get() = customNowYear.data
    var customNowYear: Customisable<Int>
        set(value) {
            val key = value.mapKey(nowYearKey)
            val saveValue = value.data
            kv.encode(key, saveValue)
        }
        get() {
            val customValue: Int = kv.decodeInt(Customisable.customKey(nowYearKey), -1)
            if (customValue != -1) {
                return Customisable(customValue, true)
            }
            val value: Int = kv.decodeInt(nowYearKey, -1)
            if (value != -1) {
                return Customisable(value, false)
            }
            // 默认值
            return Customisable(2022, false)
        }

    private val nowTermKey = "nowTerm"
    val nowTerm: Int
        get() = customNowTerm.data
    var customNowTerm: Customisable<Int>
        set(value) {
            val key = value.mapKey(nowTermKey)
            val saveValue = value.data
            kv.encode(key, saveValue)
        }
        get() {
            val customValue: Int = kv.decodeInt(Customisable.customKey(nowTermKey), -1)
            if (customValue != -1) {
                return Customisable(customValue, true)
            }
            val value: Int = kv.decodeInt(nowTermKey, -1)
            if (value != -1) {
                return Customisable(value, false)
            }
            // 默认值
            return Customisable(2, false)
        }

    private val splashListMoshi = moshi.adapter<List<Splash>>(
        Types.newParameterizedType(
            List::class.java,
            Splash::class.java
        )
    )
    private val splashListKey = "splashList"
    var splashList: List<Splash>
        set(value) {
            kv.encode(splashListKey, splashListMoshi.toJson(value))
        }
        get() {
            val saveValue = kv.decodeString(splashListKey) ?: "[]"
            return splashListMoshi.fromJson(saveValue) ?: emptyList()
        }
}