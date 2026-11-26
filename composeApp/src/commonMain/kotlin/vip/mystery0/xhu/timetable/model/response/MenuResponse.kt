package vip.mystery0.xhu.timetable.model.response

import kotlinx.serialization.Serializable
import vip.mystery0.xhu.timetable.config.store.Menu

@Serializable
data class MenuResponse(
    var title: String,
    var key: String,
    var sort: Int,
    var group: Int,
    var hint: String,
    var link: String,
    val icon: String? = null,
) {
    fun toMenu(): Menu =
        Menu(
            key = key,
            title = title,
            sort = sort,
            group = group,
            hint = hint,
            link = link,
            icon = icon,
        )
}
