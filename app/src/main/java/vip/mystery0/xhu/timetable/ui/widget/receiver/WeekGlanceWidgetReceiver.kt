package vip.mystery0.xhu.timetable.ui.widget.receiver

import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import vip.mystery0.xhu.timetable.ui.widget.WeekGlanceAppWidget

class WeekGlanceWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget
        get() = WeekGlanceAppWidget()
}