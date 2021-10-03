package vip.mystery0.xhu.timetable.config

import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.tencent.mmkv.MMKV
import vip.mystery0.xhu.timetable.model.response.Splash
import java.io.File
import java.time.*

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

    var currentYear: String
        set(value) {
            kv.encode("currentYear", value)
        }
        get() {
            val decodeString = kv.decodeString("currentYear", "")
            if (!decodeString.isNullOrBlank()) {
                return decodeString
            }
            val time = LocalDateTime.ofInstant(termStartTime, chinaZone)
            return if (time.month < Month.JUNE) {
                "${time.year - 1}-${time.year}"
            } else {
                "${time.year}-${time.year + 1}"
            }
        }

    var currentTerm: Int
        set(value) {
            kv.encode("currentTerm", value)
        }
        get() {
            val decode = kv.decodeInt("currentTerm", -1)
            if (decode != -1) {
                return decode
            }
            val time = LocalDateTime.ofInstant(termStartTime, chinaZone)
            return if (time.month < Month.JUNE) {
                2
            } else {
                1
            }
        }

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
    var lastSyncCourse: LocalDate
        set(value) {
            kv.encode(
                "lastSyncCourse",
                value.atStartOfDay().atZone(chinaZone).toInstant().toEpochMilli()
            )
        }
        get() {
            val decodeLong = kv.decodeLong("lastSyncCourse")
            return if (decodeLong != 0L) {
                LocalDateTime.ofInstant(Instant.ofEpochMilli(decodeLong), chinaZone).toLocalDate()
            } else {
                LocalDate.MIN
            }
        }
    var backgroundImage: File?
        set(value) {
            kv.encode("backgroundImage", value!!.absolutePath)
        }
        get() {
            val image = kv.decodeString("backgroundImage")
            return if (image.isNullOrBlank()) null else File(image)
        }
    var profileImage: File?
        set(value) {
            kv.encode("profileImage", value!!.absolutePath)
        }
        get() {
            val image = kv.decodeString("profileImage")
            return if (image.isNullOrBlank()) null else File(image)
        }
}