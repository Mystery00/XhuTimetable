package vip.mystery0.xhu.timetable.model.response

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable
import vip.mystery0.xhu.timetable.config.datetime.XhuInstant
import vip.mystery0.xhu.timetable.utils.asLocalDateTime

@Immutable
@Serializable
data class SponsorResponse(
    val name: String,
    val avatar: String,
    val first: XhuInstant,
    val last: XhuInstant,
    val total: String,
) {
    fun formatDateText(): String {
        val firstDate = first.asLocalDateTime().date.toString()
        val lastDate = last.asLocalDateTime().date.toString()
        return if (firstDate == lastDate) {
            "支持于 $firstDate"
        } else {
            "首次 $firstDate · 最近 $lastDate"
        }
    }

    fun formattedAmount(): String {
        val trimmed = total.trim()
        if (trimmed.isBlank()) return ""
        return if (trimmed.startsWith("¥") || trimmed.startsWith("￥")) trimmed else "¥$trimmed"
    }
}
