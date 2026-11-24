package vip.mystery0.xhu.timetable.ui.screen

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.EventNote
import androidx.compose.material.icons.rounded.AccessTime
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Book
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.datetime.format
import multiplatform.network.cmptoast.showToast
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel
import vip.mystery0.xhu.timetable.config.store.Formatter
import vip.mystery0.xhu.timetable.config.trackEvent
import vip.mystery0.xhu.timetable.model.CustomUi
import vip.mystery0.xhu.timetable.model.response.Poems
import vip.mystery0.xhu.timetable.ui.component.StateScreen
import vip.mystery0.xhu.timetable.ui.component.TabAction
import vip.mystery0.xhu.timetable.ui.component.TabContent
import vip.mystery0.xhu.timetable.ui.component.TabTitle
import vip.mystery0.xhu.timetable.ui.component.loading.LoadingButton
import vip.mystery0.xhu.timetable.ui.component.loading.LoadingValue
import vip.mystery0.xhu.timetable.ui.navigation.LocalNavController
import vip.mystery0.xhu.timetable.ui.navigation.RouteCustomCourse
import vip.mystery0.xhu.timetable.ui.navigation.RouteCustomThing
import vip.mystery0.xhu.timetable.ui.navigation.navigateAndSave
import vip.mystery0.xhu.timetable.ui.theme.CourseColorHelper
import vip.mystery0.xhu.timetable.ui.theme.XhuColor
import vip.mystery0.xhu.timetable.ui.theme.XhuIcons
import vip.mystery0.xhu.timetable.utils.copyToClipboard
import vip.mystery0.xhu.timetable.utils.formatWithDecimal
import vip.mystery0.xhu.timetable.viewmodel.CourseStatus
import vip.mystery0.xhu.timetable.viewmodel.HolidayView
import vip.mystery0.xhu.timetable.viewmodel.MainViewModel
import vip.mystery0.xhu.timetable.viewmodel.PagerMainViewModel
import vip.mystery0.xhu.timetable.viewmodel.TodayCourseSheet
import vip.mystery0.xhu.timetable.viewmodel.TodayThingSheet
import xhutimetable.composeapp.generated.resources.Res
import xhutimetable.composeapp.generated.resources.state_no_course_data

val todayCourseTitleBar: TabTitle = @Composable {
    val viewModel = koinViewModel<MainViewModel>()
    val title by viewModel.todayTitle.collectAsState()
    val showTomorrow by viewModel.showTomorrowCourse.collectAsState()

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = title.ifBlank { "今日课程" },
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
        if (showTomorrow) {
            Text(
                text = "已自动切换至明日课表",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

val todayCourseActions: TabAction = @Composable {
    val viewModel = koinViewModel<MainViewModel>()
    val pagerViewModel = koinViewModel<PagerMainViewModel>()
    val loading by viewModel.loading.collectAsState()

    if (loading) {
        LoadingButton(
            loadingValue = LoadingValue.Loading,
            modifier = Modifier.fillMaxHeight(),
        ) {}
    }
    IconButton(
        onClick = {
            pagerViewModel.showAddSheet.value = true
        },
        modifier = Modifier.fillMaxHeight(),
    ) {
        Icon(
            imageVector = Icons.Rounded.Add,
            contentDescription = "添加自定义课程或事项",
        )
    }
    false
}

@OptIn(ExperimentalMaterial3Api::class)
val todayCourseContent: TabContent = @Composable {
    val viewModel = koinViewModel<MainViewModel>()
    val openPoemsSheet = rememberSaveable { mutableStateOf(false) }
    var selectedCourse by remember { mutableStateOf<TodayCourseSheet?>(null) }

    val customUi by viewModel.customUi.collectAsState()
    val poems by viewModel.poems.collectAsState()
    val holiday by viewModel.holiday.collectAsState()
    val todayThingList by viewModel.todayThing.collectAsState()
    val todayCourseList by viewModel.todayCourse.collectAsState()
    val multiAccountMode by viewModel.multiAccountMode.collectAsState()
    val showStatus by viewModel.showStatus.collectAsState()
    val loading by viewModel.loading.collectAsState()

    val pullToRefreshState = rememberPullToRefreshState()

    Box(modifier = Modifier.fillMaxSize()) {
        PullToRefreshBox(
            isRefreshing = loading,
            onRefresh = {
                trackEvent("下拉刷新今日课表")
                viewModel.refreshCloudDataToState()
            },
            state = pullToRefreshState,
            indicator = {
                PullToRefreshDefaults.LoadingIndicator(
                    state = pullToRefreshState,
                    isRefreshing = loading,
                    modifier = Modifier.align(Alignment.TopCenter),
                )
            }
        ) {
            if (poems == null && holiday == null && todayThingList.isEmpty() && todayCourseList.isEmpty()) {
                StateScreen(
                    title = "今天没有课哦~",
                    subtitle = "可以好好放松一下，或者安排些自己的学习计划",
                    buttonText = "刷新课表",
                    imageRes = painterResource(Res.drawable.state_no_course_data),
                    onButtonClick = {
                        viewModel.refreshCloudDataToState()
                    }
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    // 顶部轻量化日程摘要胶囊
                    if (todayCourseList.isNotEmpty() || todayThingList.isNotEmpty()) {
                        item(key = "summary_header") {
                            TodaySummaryStrip(
                                courseCount = todayCourseList.size,
                                thingCount = todayThingList.size,
                                alpha = customUi.todayBackgroundAlpha,
                            )
                        }
                    }

                    // 今日诗词卡片
                    poems?.let { value ->
                        item(key = "poems_card") {
                            DrawPoemsCard(
                                customUi = customUi,
                                openBottomSheet = openPoemsSheet,
                                poems = value,
                            )
                        }
                    }

                    // 节假日横幅
                    holiday?.let { holidayItem ->
                        item(key = "holiday_card") {
                            DrawHoliday(
                                customUi = customUi,
                                holiday = holidayItem,
                            )
                        }
                    }

                    // 自定义事项与倒计时
                    if (todayThingList.isNotEmpty()) {
                        item(key = "thing_section_label") {
                            SectionHeaderLabel(text = "日程与倒计时")
                        }
                        items(todayThingList) { thing ->
                            DrawThingCard(
                                customUi = customUi,
                                thing = thing,
                                multiAccountMode = multiAccountMode,
                            )
                        }
                    }

                    // 今日课程列表
                    if (todayCourseList.isNotEmpty()) {
                        if (todayThingList.isNotEmpty()) {
                            item(key = "course_section_label") {
                                SectionHeaderLabel(text = "课程安排")
                            }
                        }
                        items(todayCourseList) { course ->
                            DrawCourseCard(
                                customUi = customUi,
                                course = course,
                                multiAccountMode = multiAccountMode,
                                showStatus = showStatus,
                                onClick = { selectedCourse = course },
                            )
                        }
                    } else if (todayThingList.isNotEmpty() || poems != null) {
                        item(key = "no_course_hint") {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(18.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = XhuColor.cardBackground.copy(alpha = customUi.todayBackgroundAlpha),
                                ),
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 28.dp, horizontal = 16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(6.dp),
                                ) {
                                    Text(
                                        text = "今日暂无课程安排",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                    )
                                    Text(
                                        text = "下拉可从云端同步最新课表",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                        }
                    }

                    item { Spacer(modifier = Modifier.height(12.dp)) }
                }
            }
        }

        TodayPoemsSheet(
            openBottomSheet = openPoemsSheet,
            poems = poems,
        )

        selectedCourse?.let { course ->
            TodayCourseDetailSheet(
                course = course,
                multiAccountMode = multiAccountMode,
                onDismiss = { selectedCourse = null },
            )
        }

        ShowAddSheet()
    }
}

@Composable
private fun SectionHeaderLabel(text: String) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.72f),
        modifier = Modifier.padding(top = 4.dp),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
        )
    }
}

@Composable
private fun TodaySummaryStrip(
    courseCount: Int,
    thingCount: Int,
    alpha: Float,
) {
    val summaryText = buildString {
        if (courseCount > 0) append("$courseCount 门课程")
        if (courseCount > 0 && thingCount > 0) append(" · ")
        if (thingCount > 0) append("$thingCount 项日程")
    }
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = (alpha * 0.92f).coerceIn(0.6f, 0.95f)),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(
                imageVector = Icons.Rounded.Schedule,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp),
            )
            Text(
                text = summaryText,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TodayPoemsSheet(
    openBottomSheet: MutableState<Boolean>,
    poems: Poems?,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    if (openBottomSheet.value && poems != null) {
        ModalBottomSheet(
            onDismissRequest = { openBottomSheet.value = false },
            sheetState = sheetState,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(start = 24.dp, end = 24.dp, bottom = 36.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                SelectionContainer {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = "《${poems.origin.title}》",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "[${poems.origin.dynasty}] ${poems.origin.author}",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.Center,
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerLow,
                        ) {
                            Text(
                                text = poems.origin.content.joinToString("\n"),
                                style = MaterialTheme.typography.bodyLarge,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                            )
                        }
                        if (!poems.origin.translate.isNullOrEmpty()) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                color = MaterialTheme.colorScheme.surfaceContainerLowest,
                                border = BorderStroke(
                                    0.5.dp,
                                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                                ),
                            ) {
                                Column(
                                    modifier = Modifier.padding(14.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp),
                                ) {
                                    Text(
                                        text = "诗词大意",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.primary,
                                    )
                                    Text(
                                        text = poems.origin.translate!!.joinToString(""),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
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

@Composable
private fun DrawPoemsCard(
    customUi: CustomUi,
    openBottomSheet: MutableState<Boolean>,
    poems: Poems,
) {
    Card(
        onClick = {
            trackEvent("点击今日诗词")
            openBottomSheet.value = true
        },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = XhuColor.cardBackground.copy(alpha = customUi.todayBackgroundAlpha),
        ),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.7f),
                modifier = Modifier.size(32.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Rounded.AutoAwesome,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onTertiaryContainer,
                        modifier = Modifier.size(16.dp),
                    )
                }
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                Text(
                    text = poems.content,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = "—— ${poems.origin.author}《${poems.origin.title}》",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.End,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun DrawHoliday(customUi: CustomUi, holiday: HolidayView) {
    val accent = CourseColorHelper.accentText(holiday.color)
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = CourseColorHelper.softContainer(holiday.color, customUi.todayBackgroundAlpha),
        ),
        border = BorderStroke(0.8.dp, accent.copy(alpha = 0.35f)),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
        ) {
            Icon(
                painter = XhuIcons.todayWaterMelon,
                contentDescription = null,
                tint = accent,
                modifier = Modifier.size(20.dp),
            )
            Text(
                text = holiday.showTitle,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = accent,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun DrawThingCard(
    customUi: CustomUi,
    thing: TodayThingSheet,
    multiAccountMode: Boolean,
) {
    val accent = CourseColorHelper.accentText(thing.color)
    var remarkExpanded by remember { mutableStateOf(false) }

    Card(
        onClick = {
            if (thing.remark.isNotBlank()) {
                remarkExpanded = !remarkExpanded
            }
        },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = XhuColor.cardBackground.copy(alpha = customUi.todayBackgroundAlpha),
        ),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .drawBehind {
                    val barWidth = 4.dp.toPx()
                    val inset = 14.dp.toPx()
                    val barHeight = (size.height - inset * 2).coerceAtLeast(0f)
                    drawRoundRect(
                        color = accent,
                        topLeft = Offset(inset, inset),
                        size = Size(barWidth, barHeight),
                        cornerRadius = CornerRadius(barWidth, barWidth),
                    )
                }
                .padding(start = 28.dp, end = 14.dp, top = 14.dp, bottom = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        text = thing.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false),
                    )
                    if (multiAccountMode && thing.accountTitle.isNotBlank()) {
                        AccountBadge(text = thing.accountTitle)
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                ) {
                    Icon(
                        imageVector = Icons.Rounded.AccessTime,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(14.dp),
                    )
                    Text(
                        text = thing.timeText,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                if (thing.location.isNotBlank()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.LocationOn,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(14.dp),
                        )
                        Text(
                            text = thing.location,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }

                if (thing.remark.isNotBlank()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 2.dp)
                            .animateContentSize(
                                animationSpec = tween(260, easing = FastOutSlowInEasing)
                            ),
                    ) {
                        Text(
                            text = thing.remark,
                            style = MaterialTheme.typography.bodySmall,
                            fontStyle = FontStyle.Normal,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f),
                            maxLines = if (remarkExpanded) Int.MAX_VALUE else 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }

            if (thing.saveAsCountDown) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = accent.copy(alpha = 0.12f),
                    border = BorderStroke(0.5.dp, accent.copy(alpha = 0.3f)),
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    ) {
                        if (thing.remainDays <= 0L) {
                            Text(
                                text = "今天",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = accent,
                            )
                        } else {
                            Text(
                                text = "还剩",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            val (number, unit) = if (thing.remainDays > 730) {
                                (thing.remainDays / 365.0).formatWithDecimal(1) to "年"
                            } else {
                                thing.remainDays.toString() to "天"
                            }
                            Text(
                                text = number,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = accent,
                            )
                            Text(
                                text = unit,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DrawCourseCard(
    customUi: CustomUi,
    course: TodayCourseSheet,
    multiAccountMode: Boolean,
    showStatus: Boolean,
    onClick: (() -> Unit)? = null,
) {
    val accent = CourseColorHelper.accentText(course.color)
    val isFinished = showStatus && course.courseStatus == CourseStatus.AFTER
    val isInProgress = showStatus && course.courseStatus == CourseStatus.IN

    val cardShape = RoundedCornerShape(18.dp)
    val cardBorder = if (isInProgress) {
        BorderStroke(1.2.dp, accent.copy(alpha = 0.65f))
    } else {
        BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
    }

    val cardContent: @Composable () -> Unit = {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
                .alpha(if (isFinished) 0.72f else 1f)
                .padding(horizontal = 14.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // 左侧时间列（保留西瓜课表特色小图标）
            Column(
                modifier = Modifier.width(46.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                Icon(
                    painter = XhuIcons.todayWaterMelon,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(16.dp),
                )
                Text(
                    text = course.timeText.first.format(Formatter.TIME_NO_SECONDS),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = course.timeText.second.format(Formatter.TIME_NO_SECONDS),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            // 彩色圆角指示条
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(4.dp))
                    .background(accent)
            )

            // 右侧主信息区
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = course.courseName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.85f),
                    ) {
                        Text(
                            text = course.timeString,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                        )
                    }
                }

                if (course.location.isNotBlank()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.LocationOn,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(14.dp),
                        )
                        Text(
                            text = course.location,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    if (course.teacherName.isNotBlank()) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Person,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(14.dp),
                            )
                            Text(
                                text = course.teacherName,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }

                    if (multiAccountMode && course.accountTitle.isNotBlank()) {
                        AccountBadge(text = course.accountTitle)
                    }

                    if (showStatus) {
                        CourseStatusBadge(status = course.courseStatus)
                    }
                }
            }
        }
    }

    if (onClick != null) {
        Card(
            onClick = onClick,
            modifier = Modifier.fillMaxWidth(),
            shape = cardShape,
            colors = CardDefaults.cardColors(
                containerColor = XhuColor.cardBackground.copy(alpha = customUi.todayBackgroundAlpha),
            ),
            border = cardBorder,
        ) {
            cardContent()
        }
    } else {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = cardShape,
            colors = CardDefaults.cardColors(
                containerColor = XhuColor.cardBackground.copy(alpha = customUi.todayBackgroundAlpha),
            ),
            border = cardBorder,
        ) {
            cardContent()
        }
    }
}

@Composable
private fun AccountBadge(text: String) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = MaterialTheme.colorScheme.secondaryContainer,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
        )
    }
}

@Composable
private fun CourseStatusBadge(status: CourseStatus) {
    val (bg, fg) = when (status) {
        CourseStatus.BEFORE -> XhuColor.Status.beforeContainer() to XhuColor.Status.beforeContent()
        CourseStatus.IN -> XhuColor.Status.inContainer() to XhuColor.Status.inContent()
        CourseStatus.AFTER -> XhuColor.Status.afterContainer() to XhuColor.Status.afterContent()
    }
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = bg,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            if (status == CourseStatus.IN) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(fg)
                )
            }
            Text(
                text = status.title,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = fg,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TodayCourseDetailSheet(
    course: TodayCourseSheet,
    multiAccountMode: Boolean,
    onDismiss: () -> Unit,
) {
    val accent = CourseColorHelper.accentText(course.color)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(
                    modifier = Modifier
                        .width(5.dp)
                        .height(40.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(accent)
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = course.courseName,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    if (multiAccountMode && course.accountTitle.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        AccountBadge(text = course.accountTitle)
                    }
                }
                CourseStatusBadge(status = course.courseStatus)
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))

            DetailInfoRow(
                icon = Icons.Rounded.AccessTime,
                label = "上课时间",
                value = "${course.timeString}  (${course.timeText.first.format(Formatter.TIME_NO_SECONDS)} - ${course.timeText.second.format(Formatter.TIME_NO_SECONDS)})",
            )
            if (course.location.isNotBlank()) {
                DetailInfoRow(
                    icon = Icons.Rounded.LocationOn,
                    label = "上课地点",
                    value = course.location,
                    copyable = true,
                )
            }
            if (course.teacherName.isNotBlank()) {
                DetailInfoRow(
                    icon = Icons.Rounded.Person,
                    label = "任课教师",
                    value = course.teacherName,
                )
            }
        }
    }
}

@Composable
internal fun DetailInfoRow(
    icon: ImageVector,
    label: String,
    value: String,
    copyable: Boolean = false,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .then(
                if (copyable) {
                    Modifier.clickable {
                        copyToClipboard(value)
                        showToast("已复制：$value")
                    }
                } else Modifier
            )
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHighest,
            modifier = Modifier.size(36.dp),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp),
                )
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
        if (copyable) {
            FilledTonalIconButton(
                onClick = {
                    copyToClipboard(value)
                    showToast("已复制")
                },
                modifier = Modifier.size(32.dp),
            ) {
                Icon(
                    imageVector = Icons.Rounded.ContentCopy,
                    contentDescription = "复制",
                    modifier = Modifier.size(15.dp),
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ShowAddSheet() {
    val pagerViewModel = koinViewModel<PagerMainViewModel>()
    val navController = LocalNavController.current
    val visible by pagerViewModel.showAddSheet.collectAsState()

    if (!visible) return

    ModalBottomSheet(
        onDismissRequest = { pagerViewModel.showAddSheet.value = false },
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = "添加日程数据",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 4.dp, bottom = 4.dp),
            )
            QuickAddActionCard(
                icon = Icons.Rounded.Book,
                title = "自定义课程",
                subtitle = "手动添加选修、旁听或蹭课安排到课表",
                onClick = {
                    pagerViewModel.showAddSheet.value = false
                    navController.navigateAndSave(RouteCustomCourse)
                }
            )
            QuickAddActionCard(
                icon = Icons.AutoMirrored.Rounded.EventNote,
                title = "自定义事项 / 倒计时",
                subtitle = "记录考试、作业截止或重要纪念日倒计时",
                onClick = {
                    pagerViewModel.showAddSheet.value = false
                    navController.navigateAndSave(RouteCustomThing)
                }
            )
        }
    }
}

@Composable
private fun QuickAddActionCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(44.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(22.dp),
                    )
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
