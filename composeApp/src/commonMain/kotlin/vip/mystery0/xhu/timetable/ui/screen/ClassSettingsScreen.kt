package vip.mystery0.xhu.timetable.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import com.maxkeppeker.sheets.core.models.base.Header
import com.maxkeppeker.sheets.core.models.base.SelectionButton
import com.maxkeppeker.sheets.core.models.base.UseCaseState
import com.maxkeppeler.sheets.calendar.CalendarDialog
import com.maxkeppeler.sheets.calendar.models.CalendarConfig
import com.maxkeppeler.sheets.calendar.models.CalendarSelection
import com.maxkeppeler.sheets.calendar.models.CalendarStyle
import com.maxkeppeler.sheets.clock.ClockDialog
import com.maxkeppeler.sheets.clock.models.ClockConfig
import com.maxkeppeler.sheets.clock.models.ClockSelection
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.format
import kotlinx.datetime.plus
import multiplatform.network.cmptoast.showToast
import org.koin.compose.viewmodel.koinViewModel
import vip.mystery0.xhu.timetable.base.HandleErrorMessage
import vip.mystery0.xhu.timetable.config.Customisable
import vip.mystery0.xhu.timetable.config.store.ConfigStore
import vip.mystery0.xhu.timetable.config.store.EventBus
import vip.mystery0.xhu.timetable.model.CampusInfo
import vip.mystery0.xhu.timetable.model.event.EventType
import vip.mystery0.xhu.timetable.ui.component.ShowSingleSelectDialog
import vip.mystery0.xhu.timetable.ui.component.preference.ConfigSettingsCheckbox
import vip.mystery0.xhu.timetable.ui.component.preference.XhuActionSettingsCheckbox
import vip.mystery0.xhu.timetable.ui.component.preference.XhuSettingsGroup
import vip.mystery0.xhu.timetable.ui.component.preference.XhuSettingsMenuLink
import vip.mystery0.xhu.timetable.ui.navigation.LocalNavController
import vip.mystery0.xhu.timetable.ui.navigation.RouteCustomCourse
import vip.mystery0.xhu.timetable.ui.navigation.RouteCustomCourseColor
import vip.mystery0.xhu.timetable.ui.navigation.RouteCustomThing
import vip.mystery0.xhu.timetable.ui.navigation.RouteSchoolCalendar
import vip.mystery0.xhu.timetable.ui.navigation.navigateAndSave
import vip.mystery0.xhu.timetable.ui.theme.XhuIcons
import vip.mystery0.xhu.timetable.utils.MIN
import vip.mystery0.xhu.timetable.utils.dateFormatter
import vip.mystery0.xhu.timetable.utils.now
import vip.mystery0.xhu.timetable.utils.timeFormatter
import vip.mystery0.xhu.timetable.viewmodel.ClassSettingsViewModel

@Composable
fun ClassSettingsScreen() {
    val viewModel = koinViewModel<ClassSettingsViewModel>()

    val navController = LocalNavController.current!!

    val currentYear by viewModel.currentYearData.collectAsState()
    val currentTerm by viewModel.currentTermData.collectAsState()
    val campusInfo by viewModel.campusInfo.collectAsState()
    val showTomorrowCourseTime by viewModel.showTomorrowCourseTimeData.collectAsState()
    val currentTermStartTime by viewModel.currentTermStartTime.collectAsState()
    val showCustomCourse by viewModel.showCustomCourseData.collectAsState()
    val showCustomThing by viewModel.showCustomThingData.collectAsState()
    val showTomorrowCourseTimeState by viewModel.showTomorrowCourseTimeState.collectAsState()
    val yearAndTermState by viewModel.yearAndTermState.collectAsState()
    val termStartTimeState by viewModel.termStartTimeState.collectAsState()
    val userCampusState by viewModel.userCampusState.collectAsState()

    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.init()
    }
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = "课程设置") },
                navigationIcon = {
                    IconButton(onClick = {
                        navController.popBackStack()
                    }) {
                        Icon(
                            painter = XhuIcons.back,
                            contentDescription = null,
                        )
                    }
                },
            )
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState()),
        ) {
            XhuSettingsGroup(title = {
                Text(text = "显示设置")
            }) {
                ConfigSettingsCheckbox(
                    config = ConfigStore::showNotThisWeek,
                    scope = scope,
                    icon = {
                        Icon(
                            painter = XhuIcons.showNotThisWeek,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface,
                        )
                    },
                    title = { Text(text = "显示非本周课程") }
                ) {
                    EventBus.post(EventType.CHANGE_SHOW_NOT_THIS_WEEK)
                }
                ConfigSettingsCheckbox(
                    config = ConfigStore::showStatus,
                    scope = scope,
                    icon = {
                        Icon(
                            painter = XhuIcons.showCourseStatus,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface,
                        )
                    },
                    title = { Text(text = "显示今日课程状态") }
                ) {
                    EventBus.post(EventType.CHANGE_SHOW_STATUS)
                }
                XhuActionSettingsCheckbox(
                    title = { Text(text = "自动显示明日课程") },
                    subtitle = {
                        Text(
                            text = if (showTomorrowCourseTime != null)
                                "在每天的 ${showTomorrowCourseTime!!.format(timeFormatter)} 后今日课程页面显示明日的课表"
                            else
                                "此功能已禁用，今日课程页面只会显示今日的课表"
                        )
                    },
                    onCheckedChange = {
                        viewModel.updateShowTomorrowCourseTime(null)
                    },
                    checkboxEnabled = showTomorrowCourseTime != null,
                    checked = showTomorrowCourseTime != null,
                    onClick = {
                        showTomorrowCourseTimeState.show()
                    }
                )
            }
            XhuSettingsGroup(title = {
                Text(text = "时间设置")
            }) {
                XhuSettingsMenuLink(
                    icon = {
                        Icon(
                            painter = XhuIcons.customYearTerm,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface,
                        )
                    },
                    title = { Text(text = "更改当前学期") },
                    subtitle = {
                        val text = buildString {
                            append("当前学期：")
                            append("${currentYear.data}学年")
                            append(" ")
                            append("第${currentTerm.data}学期")
                            if (!currentYear.custom && !currentTerm.custom) {
                                appendLine()
                                append("【根据当前时间自动计算所得】")
                            }
                        }
                        Text(text = text)
                    },
                    onClick = {
                        yearAndTermState.show()
                    }
                )
                XhuSettingsMenuLink(
                    icon = {
                        Icon(
                            painter = XhuIcons.customStartTime,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface,
                        )
                    },
                    title = { Text(text = "更改开学时间") },
                    subtitle = {
                        val text = buildString {
                            append("当前开学时间：")
                            append(currentTermStartTime.data.format(dateFormatter))
                            if (!currentTermStartTime.custom) {
                                appendLine()
                                append("【从云端自动获取】")
                            }
                        }
                        Text(text = text)
                    },
                    onClick = {
                        termStartTimeState.show()
                    }
                )
            }
            XhuSettingsGroup(title = {
                Text(text = "校区设置")
            }) {
                XhuSettingsMenuLink(
                    icon = {
                        Icon(
                            painter = XhuIcons.userCampus,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface,
                        )
                    },
                    title = { Text(text = "更新当前主用户校区") },
                    subtitle = {
                        Text(text = "当前校区：${campusInfo.selected}")
                    },
                    onClick = {
                        userCampusState.show()
                    }
                )
            }
            XhuSettingsGroup(title = {
                Text(text = "自定义设置")
            }) {
                XhuSettingsMenuLink(
                    icon = {
                        Icon(
                            painter = XhuIcons.customCourseColor,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface,
                        )
                    },
                    title = { Text(text = "自定义课程颜色") },
                    onClick = {
                        navController.navigateAndSave(RouteCustomCourseColor)
                    }
                )
            }
            XhuSettingsGroup(title = {
                Text(text = "额外功能")
            }) {
                XhuSettingsMenuLink(
                    icon = {
                        Icon(
                            painter = XhuIcons.schoolCalendar,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface,
                        )
                    },
                    title = { Text(text = "查看校历") },
                    onClick = {
                        navController.navigateAndSave(RouteSchoolCalendar)
                    }
                )
                ExportToCalendarSettings()
                XhuActionSettingsCheckbox(
                    icon = {
                        Icon(
                            painter = XhuIcons.customCourse,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface,
                        )
                    },
                    title = { Text(text = "自定义课程") },
                    subtitle = {
                        val text = if (showCustomCourse)
                            "当前：在课程表中显示自定义课程信息"
                        else
                            "当前：不显示自定义课程信息"
                        Text(text = text)
                    },
                    onCheckedChange = {
                        viewModel.updateShowCustomCourse(it)
                    },
                    checked = showCustomCourse,
                    onClick = {
                        navController.navigateAndSave(RouteCustomCourse)
                    }
                )
                XhuActionSettingsCheckbox(
                    icon = {
                        Icon(
                            painter = XhuIcons.customThing,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface,
                        )
                    },
                    title = { Text(text = "自定义事项") },
                    subtitle = {
                        val text = if (showCustomThing)
                            "当前：在今日课程页面中显示自定义事项列表"
                        else
                            "当前：不显示自定义事项列表"
                        Text(text = text)
                    },
                    onCheckedChange = {
                        viewModel.updateShowCustomThing(it)
                    },
                    checked = showCustomThing,
                    onClick = {
                        navController.navigateAndSave(RouteCustomThing)
                    }
                )
                ConfigSettingsCheckbox(
                    config = ConfigStore::showHoliday,
                    scope = scope,
                    icon = {
                        Icon(
                            painter = XhuIcons.holiday,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface,
                        )
                    },
                    title = { Text(text = "显示节假日信息") }
                ) {
                    EventBus.post(EventType.CHANGE_SHOW_HOLIDAY)
                }
            }
        }
    }
    BuildTimeSelector(
        useCaseState = showTomorrowCourseTimeState,
        initTime = showTomorrowCourseTime ?: LocalTime.now(),
        onSet = {
            viewModel.updateShowTomorrowCourseTime(it)
        }
    )
    val selectList by viewModel.selectYearAndTermList.collectAsState()
    BuildYearAndTermSelector(
        useCaseState = yearAndTermState,
        selectList = selectList,
        currentYear = currentYear,
        currentTerm = currentTerm,
        onSet = { custom, year, term ->
            viewModel.updateCurrentYearTerm(custom, year, term)
        }
    )
    BuildTermStartTimeSelector(
        useCaseState = termStartTimeState,
        initDate = currentTermStartTime.data,
        onSet = { custom, date ->
            viewModel.updateTermStartTime(custom, date)
        }
    )
    if (campusInfo != CampusInfo.EMPTY) {
        BuildUserCampusSelector(
            useCaseState = userCampusState,
            selectList = campusInfo.items,
            current = campusInfo.selected,
            onSet = {
                viewModel.updateUserCampus(it)
                showToast("当前校区已更新")
            }
        )
    }

    HandleErrorMessage(flow = viewModel.errorMessage)
}

@Composable
private fun BuildTimeSelector(
    useCaseState: UseCaseState,
    initTime: LocalTime,
    onSet: (LocalTime) -> Unit,
) {
    ClockDialog(
        header = Header.Default(
            title = "请选择时间",
        ),
        state = useCaseState,
        selection = ClockSelection.HoursMinutes { hour, minutes ->
            onSet(LocalTime(hour, minutes))
        },
        config = ClockConfig(
            defaultTime = initTime,
            is24HourFormat = true,
        )
    )
}

@Composable
private fun BuildYearAndTermSelector(
    useCaseState: UseCaseState,
    selectList: List<String>,
    currentYear: Customisable<Int>,
    currentTerm: Customisable<Int>,
    onSet: (Boolean, Int, Int) -> Unit,
) {
    val currentString =
        "${currentYear.data}-${currentYear.data + 1}学年 第${currentTerm.data}学期"
    val selectedIndex =
        if (currentYear.custom || currentTerm.custom)
            selectList.indexOf(currentString)
        else
            0

    ShowSingleSelectDialog(
        dialogTitle = "更改当前学期",
        options = selectList,
        selectIndex = selectedIndex,
        useCaseState = useCaseState,
        onSelect = { index, _ ->
            if (index == 0) {
                onSet(false, 0, 0)
            } else {
                val select = selectList[index]
                val year = select.substring(0, 4)
                val term = select.substring(13, 14)
                onSet(true, year.toInt(), term.toInt())
            }
        },
    )
}

@Composable
private fun BuildTermStartTimeSelector(
    useCaseState: UseCaseState,
    initDate: LocalDate,
    onSet: (Boolean, LocalDate) -> Unit,
) {
    CalendarDialog(
        header = Header.Default(
            title = "更改开学时间",
        ),
        state = useCaseState,
        selection = CalendarSelection.Date(
            selectedDate = initDate,
            extraButton = SelectionButton(text = "自动获取"),
            onExtraButtonClick = {
                useCaseState.hide()
                onSet(false, LocalDate.MIN)
            },
        ) {
            onSet(true, it)
        },
        config = CalendarConfig(
            cameraDate = initDate,
            yearSelection = true,
            monthSelection = true,
            style = CalendarStyle.MONTH,
            boundary = LocalDate(2015, 1, 1)..LocalDate.now().plus(1, DateTimeUnit.YEAR),
        )
    )
}

@Composable
private fun BuildUserCampusSelector(
    useCaseState: UseCaseState,
    selectList: List<String>,
    current: String,
    onSet: (String) -> Unit,
) {
    val selectedIndex = selectList.indexOf(current)

    ShowSingleSelectDialog(
        dialogTitle = "更改校区",
        options = selectList,
        selectIndex = selectedIndex,
        useCaseState = useCaseState,
        onSelect = { _, campus ->
            onSet(campus)
        }
    )
}

@Composable
expect fun ExportToCalendarSettings()