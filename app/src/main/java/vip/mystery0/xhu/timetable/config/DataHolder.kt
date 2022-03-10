package vip.mystery0.xhu.timetable.config

import androidx.compose.ui.graphics.Color
import vip.mystery0.xhu.timetable.model.response.Splash
import vip.mystery0.xhu.timetable.model.response.Version
import java.io.File

object DataHolder {
    var splashFile: File? = null
    var splash: Splash? = null
    var splashShowTime: Int = 0
    var backgroundColor: Color? = null
    var version: Version? = null
}