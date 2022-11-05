package vip.mystery0.xhu.timetable.ui.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.RowScope
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.state.GlanceStateDefinition
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import vip.mystery0.xhu.timetable.R
import vip.mystery0.xhu.timetable.ui.activity.StartActivity
import vip.mystery0.xhu.timetable.ui.widget.state.WeekCourseDataStore
import vip.mystery0.xhu.timetable.ui.widget.state.WeekCourseStateGlance
import vip.mystery0.xhu.timetable.ui.widget.state.WeekGlanceStateDefinition
import java.text.DecimalFormat
import java.time.LocalDate
import java.util.Locale

class WeekGlanceAppWidget : GlanceAppWidget() {
    companion object {
        private val weekItemHeight = 48.dp
        private val dateItemWidth = 24.dp
        private val twoFormat = DecimalFormat("00")
    }

    override val stateDefinition: GlanceStateDefinition<WeekCourseStateGlance> =
        WeekGlanceStateDefinition()

    @Composable
    override fun Content() {
        val stateGlance = currentState<WeekCourseStateGlance>()

        Box(
            modifier = GlanceModifier.padding(8.dp)
                .fillMaxSize()
                .background(color = Color.White),
        ) {
            Column {
                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = GlanceModifier.clickable(actionRunCallback<UpdateWeekCourseActionCallback>())) {
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
                        text = "查看更多 >",
                    )
                }
                Spacer(modifier = GlanceModifier.height(8.dp))
                //顶部日期栏
                Row(modifier = GlanceModifier.fillMaxWidth()) {
                    val firstDay = stateGlance.startDate
                    Text(
                        modifier = GlanceModifier.width(dateItemWidth),
                        text = "${twoFormat.format(firstDay.monthValue)}\n月",
                        style = TextStyle(
                            color = ColorProvider(Color.Black),
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Bold,
                        )
                    )
                    for (i in 0..6) {
                        val thisDay = firstDay.plusDays(i.toLong())
                        BuildDateItem(
                            week = thisDay.dayOfWeek.getDisplayName(
                                java.time.format.TextStyle.SHORT,
                                Locale.CHINESE
                            ),
                            date = if (thisDay.dayOfMonth == 1) "${twoFormat.format(thisDay.monthValue)}月" else "${
                                twoFormat.format(
                                    thisDay.dayOfMonth
                                )
                            }日",
                            isToday = LocalDate.now().dayOfWeek.value == i + 1
                        )
                    }
                }
                //课程节次列表
                LazyColumn {
                    item {
                        Row {
                            Column(
                                modifier = GlanceModifier
                                    .width(dateItemWidth)
                            ) {
                                for (time in 1..5) {
                                    BuildTimeItem(time = time)
                                }
                                BuildSingleTimeItem(time = 6)
                            }
                            if (stateGlance.hasData) {
                                for (index in 0 until 7) {
                                    Column(modifier = GlanceModifier.defaultWeight()) {
                                        stateGlance.weekCourseList[index].forEach { sheet ->
                                            if (!sheet.isEmpty()) {
                                                BuildWeekItem(
                                                    backgroundColor = sheet.color,
                                                    itemStep = sheet.step,
                                                    title = sheet.showTitle,
                                                    textColor = sheet.textColor,
                                                    showMore = sheet.course.size > 1,
                                                )
                                            } else {
                                                Spacer(
                                                    modifier = GlanceModifier
                                                        .fillMaxWidth()
                                                        .height(weekItemHeight * sheet.step),
                                                )
                                            }
                                        }
                                    }
                                }
                            } else {
                                Column(
                                    modifier = GlanceModifier.fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                ) {
                                    Image(
                                        provider = ImageProvider(R.drawable.ic_no_data),
                                        contentDescription = null,
                                    )
                                    Text(
                                        text = "暂无数据",
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
    }

    @Composable
    private fun BuildWeekItem(
        backgroundColor: Color,
        itemStep: Int,
        title: String,
        textColor: Color,
        showMore: Boolean,
    ) {
        Box(
            modifier = GlanceModifier
                .padding(1.dp)
                .fillMaxWidth()
                .height(weekItemHeight * itemStep),
            contentAlignment = Alignment.BottomEnd,
        ) {
            Text(
                modifier = GlanceModifier.fillMaxSize()
                    .background(ColorProvider(backgroundColor)),
                text = title,
                style = TextStyle(
                    color = ColorProvider(textColor),
                    textAlign = TextAlign.Center,
                    fontSize = 10.sp,
                ),
            )
            if (showMore) {
                Image(
                    provider = ImageProvider(R.drawable.ic_radius_cell),
                    contentDescription = null,
                    modifier = GlanceModifier
                        .size(6.dp),
                )
            }
        }
    }

    @Composable
    private fun RowScope.BuildDateItem(week: String, date: String, isToday: Boolean = false) {
        Text(
            modifier = GlanceModifier.defaultWeight(),
            text = "${week}\n${date}",
            style = TextStyle(
                color = ColorProvider(if (isToday) Color.Black else Color.Gray),
                textAlign = TextAlign.Center,
                fontSize = 12.sp,
                fontWeight = if (isToday) FontWeight.Bold else null,
            ),
        )
    }

    @Composable
    private fun BuildSingleTimeItem(time: Int) {
        Box(
            modifier = GlanceModifier.fillMaxWidth()
                .height(weekItemHeight),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = (2 * time - 1).toString(),
                style = TextStyle(
                    color = ColorProvider(Color.Black),
                    fontSize = 12.sp
                )
            )
        }
    }

    @Composable
    private fun BuildTimeItem(time: Int) {
        Column(
            modifier = GlanceModifier.fillMaxWidth()
                .height(weekItemHeight * 2),
        ) {
            Box(
                modifier = GlanceModifier.fillMaxWidth()
                    .height(weekItemHeight),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = (2 * time - 1).toString(),
                    style = TextStyle(
                        color = ColorProvider(Color.Black),
                        fontSize = 12.sp
                    )
                )
            }
            Box(
                modifier = GlanceModifier.fillMaxWidth()
                    .height(weekItemHeight),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = (2 * time).toString(),
                    style = TextStyle(
                        color = ColorProvider(Color.Black),
                        fontSize = 12.sp
                    )
                )
            }
        }
    }

    inner class UpdateWeekCourseActionCallback : ActionCallback {
        override suspend fun onAction(
            context: Context,
            glanceId: GlanceId,
            parameters: ActionParameters
        ) {
            WeekGlanceAppWidget().update(context, glanceId)
        }
    }
}