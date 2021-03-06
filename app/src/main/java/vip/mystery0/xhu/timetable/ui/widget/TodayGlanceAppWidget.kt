package vip.mystery0.xhu.timetable.ui.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.*
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.layout.*
import androidx.glance.state.GlanceStateDefinition
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import vip.mystery0.xhu.timetable.R
import vip.mystery0.xhu.timetable.ui.activity.StartActivity
import vip.mystery0.xhu.timetable.ui.widget.state.CourseGlance
import vip.mystery0.xhu.timetable.ui.widget.state.TodayCourseStateGlance
import vip.mystery0.xhu.timetable.ui.widget.state.TodayGlanceStateDefinition

class TodayGlanceAppWidget : GlanceAppWidget() {
    override val stateDefinition: GlanceStateDefinition<TodayCourseStateGlance> =
        TodayGlanceStateDefinition()

    @Composable
    override fun Content() {
        val stateGlance = currentState<TodayCourseStateGlance>()

        Box(
            modifier = GlanceModifier.padding(16.dp)
                .fillMaxSize()
                .background(color = Color.White),
        ) {
            Column {
                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = GlanceModifier.clickable(actionRunCallback<UpdateTodayCourseActionCallback>())) {
                        val dateString = stateGlance.date.let {
                            buildString {
                                append(it.year)
                                append("/")
                                append(it.monthValue)
                                append("/")
                                append(it.dayOfMonth)
                            }
                        }
                        Text(
                            text = dateString,
                            style = TextStyle(
                                color = ColorProvider(Color.Black),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                            ),
                        )
                        Text(
                            text = stateGlance.timeTitle,
                            style = TextStyle(
                                color = ColorProvider(Color.Black),
                                fontWeight = FontWeight.Bold,
                            ),
                        )
                    }
                    Spacer(modifier = GlanceModifier.padding(8.dp).defaultWeight())
                    Text(
                        modifier = GlanceModifier.clickable(actionStartActivity<StartActivity>()),
                        text = "???????????? >",
                    )
                }
                Spacer(modifier = GlanceModifier.height(8.dp))
                LazyColumn(modifier = GlanceModifier.fillMaxSize()) {
                    if (stateGlance.hasData) {
                        items(stateGlance.todayCourseList, CourseGlance::courseId) {
                            BuildCourseItem(todayCourseGlance = it)
                        }
                    } else {
                        item {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Image(
                                    provider = ImageProvider(R.drawable.ic_no_data),
                                    contentDescription = null,
                                )
                                Text(
                                    text = "????????????",
                                    style = TextStyle(
                                        color = ColorProvider(Color.Black),
                                        fontSize = 16.sp,
                                    ),
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun BuildCourseItem(todayCourseGlance: CourseGlance) {
        Box(modifier = GlanceModifier.padding(vertical = 4.dp)) {
            Row(
                modifier = GlanceModifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    .background(color = todayCourseGlance.color.copy(alpha = 0.5F))
                    .cornerRadius(4.dp),
            ) {
                Column(modifier = GlanceModifier.fillMaxHeight()) {
                    for (i in 0 until 4) {
                        Image(
                            provider = ImageProvider(R.drawable.ic_circle),
                            contentDescription = null,
                            modifier = GlanceModifier.size(8.dp)
                                .defaultWeight(),
                        )
                    }
                }
                Spacer(modifier = GlanceModifier.width(8.dp))
                Row(
                    modifier = GlanceModifier.padding(vertical = 8.dp)
                        .fillMaxWidth()
                        .wrapContentHeight(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        modifier = GlanceModifier.wrapContentSize(),
                        text = todayCourseGlance.time,
                        style = TextStyle(
                            color = ColorProvider(Color.Black),
                            textAlign = TextAlign.Center,
                        ),
                        maxLines = 2,
                    )
                    Spacer(modifier = GlanceModifier.width(8.dp))
                    Text(
                        modifier = GlanceModifier.wrapContentHeight()
                            .defaultWeight(),
                        text = todayCourseGlance.courseName,
                        style = TextStyle(
                            color = ColorProvider(Color.Black),
                            textAlign = TextAlign.Center,
                        ),
                        maxLines = 2,
                    )
                    Spacer(modifier = GlanceModifier.width(8.dp))
                    Text(
                        modifier = GlanceModifier.wrapContentHeight()
                            .width(128.dp),
                        text = todayCourseGlance.location,
                        style = TextStyle(
                            color = ColorProvider(Color.Black),
                            textAlign = TextAlign.Center,
                        ),
                        maxLines = 2,
                    )
                }
            }
        }
    }
}

class UpdateTodayCourseActionCallback : ActionCallback {
    override suspend fun onRun(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        // Update the count in the widget
        TodayGlanceAppWidget().update(context, glanceId)
    }
}