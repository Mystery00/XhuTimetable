package vip.mystery0.xhu.timetable.ui.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.LocalContext
import androidx.glance.LocalSize
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
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
import vip.mystery0.xhu.timetable.ui.widget.callback.UpdateTodayCourseActionCallback
import vip.mystery0.xhu.timetable.ui.widget.state.CourseGlance
import vip.mystery0.xhu.timetable.ui.widget.state.TodayCourseStateGlance
import vip.mystery0.xhu.timetable.ui.widget.state.TodayGlanceStateDefinition

class TodayGlanceAppWidget : GlanceAppWidget() {
    companion object {
        private val SMALL_SIZE = DpSize(120.dp, 110.dp)
        private val MEDIUM_SIZE = DpSize(240.dp, 110.dp)
        private val LARGE_SIZE = DpSize(240.dp, 220.dp)
    }

    override val sizeMode: SizeMode = SizeMode.Responsive(setOf(SMALL_SIZE, MEDIUM_SIZE, LARGE_SIZE))

    override val stateDefinition: GlanceStateDefinition<TodayCourseStateGlance> =
        TodayGlanceStateDefinition()

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            Content()
        }
    }

    @Composable
    private fun Content() {
        val state = currentState<TodayCourseStateGlance>()
        val context = LocalContext.current
        val colors = WidgetTheme.getColors(context)
        val size = LocalSize.current
        val isCompact = size.width < 200.dp

        if (isCompact) {
            NextCourseCardContent(
                state = state,
                colors = colors,
                onRefreshAction = actionRunCallback<UpdateTodayCourseActionCallback>(),
            )
        } else {
            TodayListContent(state = state, colors = colors)
        }
    }

    @Composable
    private fun TodayListContent(
        state: TodayCourseStateGlance,
        colors: WidgetColors,
    ) {
        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(colors.surface)
                .cornerRadius(20.dp)
                .padding(14.dp),
        ) {
            Column(modifier = GlanceModifier.fillMaxSize()) {
                // 顶部标题栏：左击刷新，右击打开主应用
                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(
                        modifier = GlanceModifier
                            .defaultWeight()
                            .clickable(actionRunCallback<UpdateTodayCourseActionCallback>()),
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "今日课程",
                                style = TextStyle(
                                    color = colors.onSurface,
                                    fontSize = 15.sp,
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
                                    text = state.weekBadgeText,
                                    style = TextStyle(
                                        color = colors.onPrimaryContainer,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Medium,
                                    ),
                                )
                            }
                        }
                        Spacer(modifier = GlanceModifier.height(2.dp))
                        Text(
                            text = state.dateLabel,
                            style = TextStyle(
                                color = colors.onSurfaceVariant,
                                fontSize = 11.sp,
                            ),
                        )
                    }
                    Box(
                        modifier = GlanceModifier
                            .clickable(actionStartActivity(androidAppConfiguration.launcherActivity))
                            .padding(horizontal = 4.dp, vertical = 4.dp),
                    ) {
                        Text(
                            text = "打开 ›",
                            style = TextStyle(
                                color = colors.primary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                            ),
                        )
                    }
                }

                Spacer(modifier = GlanceModifier.height(8.dp))

                when {
                    state.isLoggedOut -> {
                        WidgetEmptyBlock(
                            title = "打开西瓜课表同步课程",
                            colors = colors,
                        )
                    }

                    state.remainingItems.isEmpty() -> {
                        val emptyText = if (state.hasCourseToday) {
                            "今日课程已全部结束"
                        } else {
                            "今天没有课程，享受自由时光"
                        }
                        WidgetEmptyBlock(
                            title = emptyText,
                            colors = colors,
                        )
                    }

                    else -> {
                        LazyColumn(modifier = GlanceModifier.fillMaxSize()) {
                            items(state.remainingItems, CourseGlance::courseId) { item ->
                                BuildCourseRow(
                                    item = item,
                                    nowMillis = state.nowMillis,
                                    colors = colors,
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun BuildCourseRow(
        item: CourseGlance,
        nowMillis: Long,
        colors: WidgetColors,
    ) {
        val ongoing = item.isOngoing(nowMillis)
        Box(
            modifier = GlanceModifier
                .fillMaxWidth()
                .padding(vertical = 3.dp)
                .clickable(actionStartActivity(androidAppConfiguration.launcherActivity)),
        ) {
            Row(
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .background(colors.surfaceContainer)
                    .cornerRadius(12.dp)
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // 左侧彩色圆角竖条（与 iOS TodayWidgetView 和主界面视觉统一）
                Box(
                    modifier = GlanceModifier
                        .width(4.dp)
                        .height(30.dp)
                        .background(FixedColorProvider(item.color))
                        .cornerRadius(2.dp),
                ) {}

                Spacer(modifier = GlanceModifier.width(9.dp))

                Column(modifier = GlanceModifier.defaultWeight()) {
                    Text(
                        text = item.courseName,
                        style = TextStyle(
                            color = colors.onSurface,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                        ),
                        maxLines = 1,
                    )
                    Spacer(modifier = GlanceModifier.height(1.dp))
                    val sub = item.location.ifBlank { if (item.isThing) "自定义事项" else "课程" }
                    Text(
                        text = if (ongoing) "正在上课 · $sub" else sub,
                        style = TextStyle(
                            color = if (ongoing) FixedColorProvider(item.color) else colors.onSurfaceVariant,
                            fontSize = 11.sp,
                        ),
                        maxLines = 1,
                    )
                }

                Spacer(modifier = GlanceModifier.width(8.dp))

                Text(
                    text = item.time,
                    style = TextStyle(
                        color = if (item.isThing) FixedColorProvider(item.color) else colors.onSurfaceVariant,
                        fontSize = 11.sp,
                        fontWeight = if (item.isThing || ongoing) FontWeight.Medium else FontWeight.Normal,
                        textAlign = TextAlign.End,
                    ),
                    maxLines = 2,
                )
            }
        }
    }
}

@Composable
internal fun WidgetEmptyBlock(
    title: String,
    colors: WidgetColors,
) {
    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .clickable(actionStartActivity(androidAppConfiguration.launcherActivity)),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = title,
            style = TextStyle(
                color = colors.onSurfaceVariant,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
            ),
        )
    }
}
