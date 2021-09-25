package vip.mystery0.xhu.timetable.config

import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.tencent.mmkv.MMKV
import vip.mystery0.xhu.timetable.model.response.Splash
import java.time.Instant
import java.time.ZoneId

val chinaZone = ZoneId.of("Asia/Shanghai")

object Config {
    private val kv = MMKV.defaultMMKV()
    private val moshi = Moshi.Builder().build()

    var firstEnter: Boolean
        set(value) {
            kv.encode("firstEnter", value)
        }
        get() = kv.decodeBool("firstEnter", true)

    var lastVersionCode: Long
        set(value) {
            kv.encode("lastVersionCode", value)
        }
        get() = kv.decodeLong("lastVersionCode")

    var termStartTime: Instant
        set(value) {
            kv.encode("termStartTime", value.toEpochMilli())
        }
        get() = Instant.ofEpochMilli(kv.decodeLong("termStartTime", 0L))

    var splashList: List<Splash>
        set(value) {
            kv.encode(
                "splashList",
                moshi.adapter<List<Splash>>(
                    Types.newParameterizedType(
                        List::class.java,
                        Splash::class.java
                    )
                ).toJson(value)
            )
        }
        get() = moshi.adapter<List<Splash>>(
            Types.newParameterizedType(
                List::class.java,
                Splash::class.java
            )
        ).fromJson(kv.decodeString("splashList", "[]")!!)!!

    var userList: List<User>
        set(value) {
            kv.encode(
                "userList",
                moshi.adapter<List<User>>(
                    Types.newParameterizedType(
                        List::class.java,
                        User::class.java
                    )
                ).toJson(value)
            )
        }
        get() = moshi.adapter<List<User>>(
            Types.newParameterizedType(
                List::class.java,
                User::class.java
            )
        ).fromJson(kv.decodeString("userList", "[]")!!)!!
    var poemsToken: String?
        set(value) {
            kv.encode("poemsToken", value)
        }
        get() = kv.decodeString("poemsToken")
}