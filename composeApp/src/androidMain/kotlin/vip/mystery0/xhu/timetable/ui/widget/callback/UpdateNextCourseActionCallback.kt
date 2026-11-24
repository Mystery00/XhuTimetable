package vip.mystery0.xhu.timetable.ui.widget.callback

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback
import vip.mystery0.xhu.timetable.ui.widget.NextCourseGlanceAppWidget

class UpdateNextCourseActionCallback : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        NextCourseGlanceAppWidget().update(context, glanceId)
    }
}
