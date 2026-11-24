package vip.mystery0.xhu.timetable.ui.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.LocalContext
import androidx.glance.action.Action
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.cornerRadius
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
import androidx.glance.text.TextStyle
import androidx.glance.unit.FixedColorProvider
import vip.mystery0.xhu.timetable.androidAppConfiguration
import vip.mystery0.xhu.timetable.ui.widget.callback.UpdateNextCourseActionCallback
import vip.mystery0.xhu.timetable.ui.widget.state.TodayCourseStateGlance
import vip.mystery0.xhu.timetable.ui.widget.state.TodayGlanceStateDefinition

/**
 * 2x2 精简「下一节课」小组件（对齐 iOS systemSmall 聚焦卡片）
 */
class NextCourseGlanceAppWidget : GlanceAppWidget() {
    override val stateDefinition: GlanceStateDefinition<TodayCourseStateGlance> =
        TodayGlanceStateDefinition()

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            val state = currentState<TodayCourseStateGlance>()
            val colors = WidgetTheme.getColors(LocalContext.current)
            NextCourseCardContent(
                state = state,
                colors = colors,
                onRefreshAction = actionRunCallback<UpdateNextCourseActionCallback>(),
            )
        }
    }
}

@Composable
internal fun NextCourseCardContent(
    state: TodayCourseStateGlance,
    colors: WidgetColors,
    onRefreshAction: Action,
) {
    val next = state.nextCourse
    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(colors.surface)
            .cornerRadius(20.dp)
            .padding(14.dp),
    ) {
        Column(modifier = GlanceModifier.fillMaxSize()) {
            // 顶栏：标题 + 周次胶囊（点击刷新）
            Row(
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .clickable(onRefreshAction),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "下一节课",
                    style = TextStyle(
                        color = colors.onSurface,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                    ),
                )
                Spacer(modifier = GlanceModifier.defaultWeight())
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

            Spacer(modifier = GlanceModifier.height(8.dp))

            when {
                state.isLoggedOut -> {
                    WidgetEmptyBlock(
                        title = "打开西瓜课表\n同步课程",
                        colors = colors,
                    )
                }

                next == null -> {
                    WidgetEmptyBlock(
                        title = if (state.hasCourseToday) "今日课程已结束" else "今天没有课程\n享受自由时光",
                        colors = colors,
                    )
                }

                else -> {
                    val ongoing = next.isOngoing(state.nowMillis)
                    Column(
                        modifier = GlanceModifier
                            .fillMaxSize()
                            .clickable(actionStartActivity(androidAppConfiguration.launcherActivity)),
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = GlanceModifier
                                    .width(4.dp)
                                    .height(14.dp)
                                    .background(FixedColorProvider(next.color))
                                    .cornerRadius(2.dp),
                            ) {}
                            Spacer(modifier = GlanceModifier.width(6.dp))
                            Text(
                                text = if (ongoing) "正在上课" else "即将开始",
                                style = TextStyle(
                                    color = FixedColorProvider(next.color),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                ),
                            )
                        }

                        Spacer(modifier = GlanceModifier.height(6.dp))

                        Text(
                            text = next.courseName,
                            style = TextStyle(
                                color = colors.onSurface,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                            ),
                            maxLines = 2,
                        )

                        if (next.location.isNotBlank()) {
                            Spacer(modifier = GlanceModifier.height(3.dp))
                            Text(
                                text = next.location,
                                style = TextStyle(
                                    color = colors.onSurfaceVariant,
                                    fontSize = 11.sp,
                                ),
                                maxLines = 1,
                            )
                        }

                        Spacer(modifier = GlanceModifier.defaultWeight())

                        Box(
                            modifier = GlanceModifier
                                .fillMaxWidth()
                                .background(colors.surfaceContainer)
                                .cornerRadius(10.dp)
                                .padding(horizontal = 8.dp, vertical = 5.dp),
                        ) {
                            Text(
                                text = next.time,
                                style = TextStyle(
                                    color = colors.onSurface,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                ),
                                maxLines = 1,
                            )
                        }
                    }
                }
            }
        }
    }
}
