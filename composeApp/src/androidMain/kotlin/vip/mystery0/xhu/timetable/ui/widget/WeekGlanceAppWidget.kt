package vip.mystery0.xhu.timetable.ui.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.LocalContext
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.provideContent
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
import androidx.glance.layout.width
import androidx.glance.state.GlanceStateDefinition
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import androidx.glance.unit.FixedColorProvider
import vip.mystery0.xhu.timetable.androidAppConfiguration
import vip.mystery0.xhu.timetable.ui.theme.XhuColor
import vip.mystery0.xhu.timetable.ui.widget.callback.UpdateWeekCourseActionCallback
import vip.mystery0.xhu.timetable.ui.widget.state.WeekCourseStateGlance
import vip.mystery0.xhu.timetable.ui.widget.state.WeekGlanceStateDefinition
import java.text.DecimalFormat
import java.time.LocalDate

class WeekGlanceAppWidget : GlanceAppWidget() {
    companion object {
        private val weekItemHeight = 42.dp
        private val dateItemWidth = 20.dp
        private val twoFormat = DecimalFormat("00")
        private val weekdayLabels = listOf("一", "二", "三", "四", "五", "六", "日")
    }

    override val stateDefinition: GlanceStateDefinition<WeekCourseStateGlance> =
        WeekGlanceStateDefinition()

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            Content()
        }
    }

    @Composable
    private fun Content() {
        val stateGlance = currentState<WeekCourseStateGlance>()
        val context = LocalContext.current
        val colors = WidgetTheme.getColors(context)
        val today = LocalDate.now()

        val weekBadgeText = when {
            stateGlance.currentWeek <= 0 -> "未开学"
            stateGlance.currentWeek > 20 -> "已结课"
            else -> "第${stateGlance.currentWeek}周"
        }

        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(colors.surface)
                .cornerRadius(20.dp)
                .padding(12.dp),
        ) {
            Column(modifier = GlanceModifier.fillMaxSize()) {
                // 顶栏：本周课表 + 周次胶囊（点击刷新） + 打开入口
                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(
                        modifier = GlanceModifier
                            .defaultWeight()
                            .clickable(actionRunCallback<UpdateWeekCourseActionCallback>()),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "本周课表",
                            style = TextStyle(
                                color = colors.onSurface,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                            ),
                        )
                        Spacer(modifier = GlanceModifier.width(6.dp))
                        Box(
                            modifier = GlanceModifier
                                .background(colors.primaryContainer)
                                .cornerRadius(8.dp)
                                .padding(horizontal = 6.dp, vertical = 2.dp),
                        ) {
                            Text(
                                text = weekBadgeText,
                                style = TextStyle(
                                    color = colors.onPrimaryContainer,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium,
                                ),
                            )
                        }
                    }
                    Text(
                        modifier = GlanceModifier
                            .clickable(actionStartActivity(androidAppConfiguration.launcherActivity))
                            .padding(horizontal = 4.dp, vertical = 2.dp),
                        text = "打开 ›",
                        style = TextStyle(
                            color = colors.primary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                        ),
                    )
                }

                Spacer(modifier = GlanceModifier.height(6.dp))

                // 星期日期栏（今日圆形高亮胶囊，与主课表及 iOS 小部件风格一致）
                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    val firstDay = stateGlance.startDate
                    Text(
                        modifier = GlanceModifier.width(dateItemWidth),
                        text = "${firstDay.monthValue}\n月",
                        style = TextStyle(
                            color = colors.primary,
                            fontSize = 9.sp,
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Bold,
                        ),
                    )
                    for (i in 0..6) {
                        val thisDay = firstDay.plusDays(i.toLong())
                        BuildDateItem(
                            week = weekdayLabels[i],
                            date = if (thisDay.dayOfMonth == 1) {
                                "${thisDay.monthValue}月"
                            } else {
                                twoFormat.format(thisDay.dayOfMonth)
                            },
                            isToday = thisDay == today,
                            colors = colors,
                        )
                    }
                }

                Spacer(modifier = GlanceModifier.height(4.dp))

                // 课程网格主体
                if (!stateGlance.hasData) {
                    WidgetEmptyBlock(
                        title = "本周暂无课程安排",
                        colors = colors,
                    )
                } else {
                    LazyColumn(modifier = GlanceModifier.fillMaxSize()) {
                        item {
                            Row(
                                modifier = GlanceModifier
                                    .fillMaxWidth()
                                    .clickable(actionStartActivity(androidAppConfiguration.launcherActivity)),
                            ) {
                                Column(modifier = GlanceModifier.width(dateItemWidth)) {
                                    for (period in 1..11) {
                                        Box(
                                            modifier = GlanceModifier
                                                .fillMaxWidth()
                                                .height(weekItemHeight),
                                            contentAlignment = Alignment.Center,
                                        ) {
                                            Text(
                                                text = period.toString(),
                                                style = TextStyle(
                                                    color = colors.onSurfaceVariant,
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Medium,
                                                ),
                                            )
                                        }
                                    }
                                }
                                for (index in 0 until 7) {
                                    Column(modifier = GlanceModifier.defaultWeight()) {
                                        stateGlance.weekCourseList.getOrNull(index)?.forEach { sheet ->
                                            if (!sheet.isEmpty()) {
                                                BuildWeekItem(
                                                    backgroundColor = sheet.color,
                                                    itemStep = sheet.step,
                                                    title = sheet.showTitle,
                                                    conflictCount = (sheet.course.size - 1).coerceAtLeast(0),
                                                    colors = colors,
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
        conflictCount: Int,
        colors: WidgetColors,
    ) {
        val isNotThisWeek = backgroundColor == XhuColor.notThisWeekBackgroundColor
        val cellBg = if (isNotThisWeek) {
            colors.surfaceContainer
        } else {
            FixedColorProvider(backgroundColor.copy(alpha = if (colors.isDark) 0.78f else 0.88f))
        }
        val cellFg = if (isNotThisWeek) {
            colors.onSurfaceVariant
        } else {
            FixedColorProvider(Color.White)
        }

        Box(
            modifier = GlanceModifier
                .fillMaxWidth()
                .height(weekItemHeight * itemStep)
                .padding(1.5.dp),
        ) {
            Box(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(cellBg)
                    .cornerRadius(6.dp)
                    .padding(horizontal = 2.dp, vertical = 3.dp),
                contentAlignment = Alignment.TopCenter,
            ) {
                Text(
                    text = if (conflictCount > 0) "$title\n+$conflictCount" else title,
                    style = TextStyle(
                        color = cellFg,
                        textAlign = TextAlign.Center,
                        fontSize = 9.sp,
                        fontWeight = if (isNotThisWeek) FontWeight.Normal else FontWeight.Medium,
                    ),
                )
            }
        }
    }

    @Composable
    private fun RowScope.BuildDateItem(
        week: String,
        date: String,
        isToday: Boolean = false,
        colors: WidgetColors,
    ) {
        Column(
            modifier = GlanceModifier.defaultWeight(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = week,
                style = TextStyle(
                    color = if (isToday) colors.primary else colors.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    fontSize = 9.sp,
                    fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                ),
            )
            Box(
                modifier = GlanceModifier
                    .then(
                        if (isToday) {
                            GlanceModifier
                                .background(colors.primary)
                                .cornerRadius(8.dp)
                                .padding(horizontal = 5.dp, vertical = 1.dp)
                        } else {
                            GlanceModifier.padding(horizontal = 2.dp, vertical = 1.dp)
                        }
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = date,
                    style = TextStyle(
                        color = if (isToday) colors.onPrimary else colors.onSurface,
                        textAlign = TextAlign.Center,
                        fontSize = 9.sp,
                        fontWeight = if (isToday) FontWeight.Bold else FontWeight.Medium,
                    ),
                )
            }
        }
    }
}
