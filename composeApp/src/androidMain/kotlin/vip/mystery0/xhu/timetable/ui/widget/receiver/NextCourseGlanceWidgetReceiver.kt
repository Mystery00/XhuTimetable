package vip.mystery0.xhu.timetable.ui.widget.receiver

import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import vip.mystery0.xhu.timetable.ui.widget.NextCourseGlanceAppWidget

class NextCourseGlanceWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget
        get() = NextCourseGlanceAppWidget()
}
