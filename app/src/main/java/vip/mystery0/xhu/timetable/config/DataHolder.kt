package vip.mystery0.xhu.timetable.config

import vip.mystery0.xhu.timetable.model.entity.NightMode
import vip.mystery0.xhu.timetable.model.response.Version

object DataHolder {
    var version: Version? = null
    var nightMode: NightMode = NightMode.AUTO
}