package vip.mystery0.xhu.timetable.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.maxkeppeler.sheets.list.ListDialog
import com.maxkeppeler.sheets.list.models.ListOption
import com.maxkeppeler.sheets.list.models.ListSelection
import kotlinx.coroutines.CoroutineScope
import kotlinx.datetime.format
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel
import vip.mystery0.xhu.timetable.config.store.Formatter
import vip.mystery0.xhu.timetable.config.trackEvent
import vip.mystery0.xhu.timetable.model.response.Poems
import vip.mystery0.xhu.timetable.ui.component.StateScreen
import vip.mystery0.xhu.timetable.ui.component.TabAction
import vip.mystery0.xhu.timetable.ui.component.TabContent
import vip.mystery0.xhu.timetable.ui.component.TabTitle
import vip.mystery0.xhu.timetable.ui.component.loading.LoadingButton
import vip.mystery0.xhu.timetable.ui.component.loading.LoadingValue
import vip.mystery0.xhu.timetable.ui.component.xhuHeader
import vip.mystery0.xhu.timetable.ui.navigation.LocalNavController
import vip.mystery0.xhu.timetable.ui.navigation.RouteCustomCourse
import vip.mystery0.xhu.timetable.ui.navigation.RouteCustomThing
import vip.mystery0.xhu.timetable.ui.navigation.navigateAndSave
import vip.mystery0.xhu.timetable.ui.theme.ColorPool
import vip.mystery0.xhu.timetable.ui.theme.XhuColor
import vip.mystery0.xhu.timetable.ui.theme.XhuIcons
import vip.mystery0.xhu.timetable.utils.formatWithDecimal
import vip.mystery0.xhu.timetable.viewmodel.HolidayView
import vip.mystery0.xhu.timetable.viewmodel.MainViewModel
import vip.mystery0.xhu.timetable.viewmodel.PagerMainViewModel
import vip.mystery0.xhu.timetable.viewmodel.TodayCourseSheet
import vip.mystery0.xhu.timetable.viewmodel.TodayThingSheet
import xhutimetable.composeapp.generated.resources.Res
import xhutimetable.composeapp.generated.resources.state_no_course_data

val todayCourseTitleBar: TabTitle = @Composable {
    val viewModel = koinViewModel<MainViewModel>()
    val title = viewModel.todayTitle.collectAsState()

    Text(text = title.value)
}

val todayCourseActions: TabAction = @Composable {
    val viewModel = koinViewModel<MainViewModel>()
    val pagerViewModel = koinViewModel<PagerMainViewModel>()
    val userCaseState by pagerViewModel.showAddDialog.collectAsState()
    val loading by viewModel.loading.collectAsState()

    if (loading) {
        LoadingButton(
            loadingValue = LoadingValue.Loading,
            modifier = Modifier
                .fillMaxHeight(),
        ) {}
    }
    IconButton(
        onClick = {
            userCaseState.show()
        },
        modifier = Modifier
            .fillMaxHeight()
    ) {
        Icon(
            painter = XhuIcons.Action.addCircle,
            contentDescription = null,
        )
    }
    false
}

val todayCourseContent: TabContent = @Composable {
    val viewModel = koinViewModel<MainViewModel>()
    val scope = rememberCoroutineScope()
    val openBottomSheet = rememberSaveable { mutableStateOf(false) }
    Box {
        val poems by viewModel.poems.collectAsState()
        val holiday by viewModel.holiday.collectAsState()
        val todayThingList by viewModel.todayThing.collectAsState()
        val todayCourseList by viewModel.todayCourse.collectAsState()

        if (poems == null && todayThingList.isEmpty() && todayCourseList.isEmpty()) {
            StateScreen(
                title = "今天没有课哦~",
                subtitle = "哎呀，今天没有课课耶~\n可以尽情玩耍啦！",
                imageRes = painterResource(Res.drawable.state_no_course_data),
                verticalArrangement = Arrangement.Top,
            )
        } else {
            DrawLine()
            val multiAccountMode by viewModel.multiAccountMode.collectAsState()
            val showStatus by viewModel.showStatus.collectAsState()
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
            ) {
                poems?.let { value ->
                    DrawPoemsCard(openBottomSheet, scope, poems = value)
                }
                holiday?.let { holiday ->
                    DrawHoliday(holiday = holiday)
                }
                todayThingList.forEach {
                    DrawThingCard(thing = it, multiAccountMode = multiAccountMode)
                }
                todayCourseList.forEach {
                    DrawCourseCard(
                        course = it,
                        multiAccountMode = multiAccountMode,
                        showStatus = showStatus
                    )
                }
            }
        }

        TodayPoemsSheet(
            openBottomSheet = openBottomSheet,
            poems = poems,
        )

        ShowAddDialog()
    }
}

@Composable
private fun TodayPoemsSheet(
    openBottomSheet: MutableState<Boolean>,
    poems: Poems?,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    if (openBottomSheet.value && poems != null) {
        ModalBottomSheet(
            onDismissRequest = {
                openBottomSheet.value = false
            },
            sheetState = sheetState,
        ) {
            Column {
                SelectionContainer(
                    modifier = Modifier.padding(
                        top = 8.dp,
                        start = 32.dp,
                        end = 32.dp,
                        bottom = 32.dp,
                    ),
                ) {
                    Column {
                        Text(
                            text = "《${poems.origin.title}》",
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "[${poems.origin.dynasty}] ${poems.origin.author}",
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = poems.origin.content.joinToString("\n"),
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        if (!poems.origin.translate.isNullOrEmpty()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "诗词大意：${
                                    poems.origin.translate!!.joinToString(
                                        ""
                                    )
                                }",
                                fontSize = 11.sp,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                        Spacer(modifier = Modifier.heightIn(min = 16.dp, max = 24.dp))
                    }
                }
            }
        }
    }

}

@Composable
private fun DrawLine() {
    Box(
        modifier = Modifier
            .padding(horizontal = 10.dp)
            .fillMaxHeight()
            .width(1.dp)
            .background(color = Color.White)
    )
}

@Composable
private fun DrawPoemsCard(
    openBottomSheet: MutableState<Boolean>,
    scope: CoroutineScope,
    poems: Poems,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Surface(
            shape = CircleShape,
            modifier = Modifier
                .padding(horizontal = 6.dp)
                .size(9.dp),
            color = ColorPool.random
        ) {}
        Card(
            onClick = {
                trackEvent("点击今日诗词")
                openBottomSheet.value = true
            },
            modifier = Modifier
                .padding(end = 8.dp)
                .fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = XhuColor.cardBackground,
            ),
        ) {
            Column(
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth(),
            ) {
                Text(
                    text = poems.content,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
                Text(
                    text = "——${poems.origin.author}《${poems.origin.title}》",
                    fontSize = 12.sp,
                    textAlign = TextAlign.End,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun DrawHoliday(holiday: HolidayView) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Surface(
            shape = CircleShape,
            modifier = Modifier
                .padding(horizontal = 6.dp)
                .size(9.dp),
            color = holiday.color,
        ) {}
        Card(
            modifier = Modifier
                .padding(end = 8.dp)
                .fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = XhuColor.cardBackground,
            ),
        ) {
            Box {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(
                        top = 8.dp,
                        bottom = 8.dp,
                        end = 8.dp
                    ),
                ) {
                    Icon(
                        painter = XhuIcons.todayWaterMelon,
                        contentDescription = null,
                        tint = holiday.color,
                        modifier = Modifier
                            .padding(6.dp)
                            .size(16.dp),
                    )
                    Column(
                        modifier = Modifier
                            .weight(1F)
                    ) {
                        Text(
                            text = holiday.showTitle,
                            fontWeight = FontWeight.Bold,
                            color = holiday.color,
                            fontSize = 16.sp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 1.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DrawThingCard(
    thing: TodayThingSheet,
    multiAccountMode: Boolean,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Surface(
            shape = CircleShape,
            modifier = Modifier
                .padding(horizontal = 6.dp)
                .size(9.dp),
            color = thing.color,
        ) {}
        Card(
            modifier = Modifier
                .padding(end = 8.dp)
                .fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = XhuColor.cardBackground,
            ),
        ) {
            Box {
                if (multiAccountMode) {
                    Text(
                        text = thing.accountTitle,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier
                            .background(
                                color = MaterialTheme.colorScheme.secondaryContainer,
                                shape = RoundedCornerShape(bottomStart = 4.dp),
                            )
                            .padding(1.dp)
                            .align(Alignment.TopEnd),
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(
                        top = if (multiAccountMode) 18.dp else 8.dp,
                        bottom = 8.dp,
                        end = 8.dp
                    ),
                ) {
                    Icon(
                        painter = XhuIcons.todayWaterMelon,
                        contentDescription = null,
                        tint = thing.color,
                        modifier = Modifier
                            .padding(6.dp)
                            .size(16.dp),
                    )
                    Column(
                        modifier = Modifier
                            .weight(1F)
                    ) {
                        Text(
                            text = thing.title,
                            fontWeight = FontWeight.Bold,
                            color = thing.color,
                            fontSize = 16.sp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 1.dp),
                        )
                        Text(
                            text = thing.timeText,
                            color = thing.color.copy(alpha = 0.8F),
                            fontSize = 12.sp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 1.dp),
                        )
                        if (thing.location.isNotBlank()) {
                            Text(
                                text = thing.location,
                                color = thing.color.copy(alpha = 0.8F),
                                fontSize = 12.sp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 1.dp),
                            )
                        }
                        if (thing.remark.isNotBlank()) {
                            Text(
                                text = thing.remark,
                                color = thing.color.copy(alpha = 0.6F),
                                fontSize = 12.sp,
                                fontStyle = FontStyle.Italic,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 1.dp),
                            )
                        }
                    }
                    if (thing.saveAsCountDown) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(start = 4.dp),
                        ) {
                            if (thing.remainDays <= 0L) {
                                //今天
                                Text(
                                    text = "就在\n今天",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = thing.color,
                                )
                            } else {
                                //还没到
                                Text(
                                    text = "还剩",
                                    fontSize = 12.sp,
                                )
                                if (thing.remainDays > 730) {
                                    Text(
                                        text = (thing.remainDays / 365.0).formatWithDecimal(1),
                                        fontSize = 24.sp,
                                        modifier = Modifier
                                            .padding(vertical = 2.dp),
                                        textAlign = TextAlign.Center,
                                        fontWeight = FontWeight.Bold,
                                        color = thing.color,
                                    )
                                    Text(
                                        text = "年",
                                        fontSize = 12.sp,
                                    )
                                } else {
                                    Text(
                                        text = thing.remainDays.toString(),
                                        fontSize = 24.sp,
                                        modifier = Modifier
                                            .padding(vertical = 2.dp),
                                        textAlign = TextAlign.Center,
                                        fontWeight = FontWeight.Bold,
                                        color = thing.color,
                                    )
                                    Text(
                                        text = "天",
                                        fontSize = 12.sp,
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
private fun DrawCourseCard(
    course: TodayCourseSheet,
    multiAccountMode: Boolean,
    showStatus: Boolean,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Surface(
            shape = CircleShape,
            modifier = Modifier
                .padding(horizontal = 6.dp)
                .size(9.dp),
            color = course.color
        ) {}
        Card(
            modifier = Modifier
                .padding(end = 8.dp)
                .fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = XhuColor.cardBackground,
            )
        ) {
            Row(
                modifier = Modifier.height(IntrinsicSize.Min),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(
                    modifier = Modifier
                        .padding(start = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Icon(
                        painter = XhuIcons.todayWaterMelon,
                        contentDescription = null,
                        tint = course.color,
                        modifier = Modifier
                            .size(16.dp),
                    )
                    Text(
                        text = course.timeText.first.format(Formatter.TIME_NO_SECONDS),
                        fontSize = 12.sp,
                        modifier = Modifier
                            .padding(vertical = 1.dp),
                        textAlign = TextAlign.Center,
                    )
                    Text(
                        text = course.timeText.second.format(Formatter.TIME_NO_SECONDS),
                        fontSize = 12.sp,
                        modifier = Modifier
                            .padding(vertical = 1.dp),
                        textAlign = TextAlign.Center,
                    )
                }
                Surface(
                    shape = RoundedCornerShape(36.dp),
                    modifier = Modifier
                        .padding(vertical = 12.dp)
                        .fillMaxHeight()
                        .width(4.dp),
                    color = course.color
                ) {}
                Box {
                    if (multiAccountMode) {
                        Text(
                            text = course.accountTitle,
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier
                                .background(
                                    color = MaterialTheme.colorScheme.secondaryContainer,
                                    shape = RoundedCornerShape(bottomStart = 4.dp),
                                )
                                .padding(1.dp)
                                .align(Alignment.TopEnd),
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .padding(
                                top = if (multiAccountMode) 18.dp else 8.dp,
                                bottom = if (showStatus) 24.dp else 8.dp,
                                end = 8.dp
                            ),
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 1.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = course.courseName,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    modifier = Modifier.weight(1F),
                                )
                                Text(
                                    text = course.timeString,
                                    fontSize = 14.sp,
                                )
                            }
                            Text(
                                text = course.teacherName,
                                fontSize = 12.sp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 1.dp),
                            )
                            Text(
                                text = course.location,
                                fontSize = 12.sp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 1.dp),
                            )
                        }
                    }
                    if (showStatus) {
                        Text(
                            text = course.courseStatus.title,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier
                                .background(
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    shape = RoundedCornerShape(topStart = 4.dp),
                                )
                                .padding(2.dp)
                                .align(Alignment.BottomEnd),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ShowAddDialog() {
    val pagerViewModel = koinViewModel<PagerMainViewModel>()
    val navController = LocalNavController.current!!
    val userCaseState by pagerViewModel.showAddDialog.collectAsState()
    ListDialog(
        header = xhuHeader(title = "添加数据"),
        state = userCaseState,
        selection = ListSelection.Single(
            withButtonView = false,
            options = listOf(
                ListOption(titleText = "自定义课程"),
                ListOption(titleText = "自定义事项"),
            ),
            onSelectOption = { index, _ ->
                when (index) {
                    0 -> {
                        navController.navigateAndSave(RouteCustomCourse)
                    }

                    1 -> {
                        navController.navigateAndSave(RouteCustomThing)
                    }
                }
            }
        ),
    )
}