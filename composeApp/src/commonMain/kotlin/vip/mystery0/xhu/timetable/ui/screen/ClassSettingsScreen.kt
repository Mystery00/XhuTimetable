package vip.mystery0.xhu.timetable.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoMode
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.format
import multiplatform.network.cmptoast.showToast
import org.koin.compose.viewmodel.koinViewModel
import vip.mystery0.xhu.timetable.base.HandleErrorMessage
import vip.mystery0.xhu.timetable.config.store.ConfigStore
import vip.mystery0.xhu.timetable.config.store.EventBus
import vip.mystery0.xhu.timetable.model.CampusInfo
import vip.mystery0.xhu.timetable.model.TermStartChangeAlert
import vip.mystery0.xhu.timetable.model.event.EventType
import vip.mystery0.xhu.timetable.ui.component.dialog.XhuDatePickerDialog
import vip.mystery0.xhu.timetable.ui.component.dialog.XhuSingleSelectDialog
import vip.mystery0.xhu.timetable.ui.component.dialog.XhuTimePickerDialog
import vip.mystery0.xhu.timetable.ui.component.dialog.rememberXhuDialogState
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
import vip.mystery0.xhu.timetable.utils.timeFormatter
import vip.mystery0.xhu.timetable.viewmodel.ClassSettingsViewModel

@Composable
fun ClassSettingsScreen() {
    val viewModel = koinViewModel<ClassSettingsViewModel>()
    val navController = LocalNavController.current

    val currentYear by viewModel.currentYearData.collectAsState()
    val currentTerm by viewModel.currentTermData.collectAsState()
    val campusInfo by viewModel.campusInfo.collectAsState()
    val showTomorrowCourseTime by viewModel.showTomorrowCourseTimeData.collectAsState()
    val currentTermStartTime by viewModel.currentTermStartTime.collectAsState()
    val serverTermStartDate by viewModel.serverTermStartDate.collectAsState()
    val showCustomCourse by viewModel.showCustomCourseData.collectAsState()
    val showCustomThing by viewModel.showCustomThingData.collectAsState()
    val selectList by viewModel.selectYearAndTermList.collectAsState()

    val tomorrowTimeDialog = rememberXhuDialogState()
    val yearTermDialog = rememberXhuDialogState()
    val termStartDialog = rememberXhuDialogState()
    val campusDialog = rememberXhuDialogState()

    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.init()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "课程设置",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            painter = XhuIcons.back,
                            contentDescription = "返回",
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
            XhuSettingsGroup(title = { Text("显示设置") }) {
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
                    title = { Text("显示非本周课程") },
                    subtitle = { Text("在周课表中以灰阶展示非当前周次的课程") },
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
                    title = { Text("显示今日课程状态") },
                    subtitle = { Text("标注课程的未开始、开课中、已结束状态") },
                ) {
                    EventBus.post(EventType.CHANGE_SHOW_STATUS)
                }
                XhuActionSettingsCheckbox(
                    icon = {
                        Icon(
                            imageVector = Icons.Rounded.AutoMode,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface,
                        )
                    },
                    title = { Text("自动显示明日课程") },
                    subtitle = {
                        Text(
                            text = if (showTomorrowCourseTime != null) {
                                "每天 ${showTomorrowCourseTime!!.format(timeFormatter)} 后自动切换显示明日课表"
                            } else {
                                "已关闭，点击开关可开启并设定切换时间"
                            }
                        )
                    },
                    onCheckedChange = { checked ->
                        if (checked) {
                            tomorrowTimeDialog.show()
                        } else {
                            viewModel.updateShowTomorrowCourseTime(null)
                        }
                    },
                    checkboxEnabled = true,
                    checked = showTomorrowCourseTime != null,
                    onClick = { tomorrowTimeDialog.show() }
                )
            }

            XhuSettingsGroup(title = { Text("学期与时间") }) {
                XhuSettingsMenuLink(
                    icon = {
                        Icon(
                            painter = XhuIcons.customYearTerm,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface,
                        )
                    },
                    title = { Text("更改当前学期") },
                    subtitle = {
                        val suffix = if (!currentYear.custom && !currentTerm.custom) "（自动计算）" else "（已自定义）"
                        Text("${currentYear.data}学年 第${currentTerm.data}学期 $suffix")
                    },
                    onClick = { yearTermDialog.show() }
                )
                XhuSettingsMenuLink(
                    icon = {
                        Icon(
                            painter = XhuIcons.customStartTime,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface,
                        )
                    },
                    title = { Text("更改开学时间") },
                    subtitle = {
                        val text = buildString {
                            if (currentTermStartTime.custom) {
                                append("当前设置：${currentTermStartTime.data.format(dateFormatter)}（已自定义）")
                                appendLine()
                                val diffDays =
                                    currentTermStartTime.data.toEpochDays() - serverTermStartDate.toEpochDays()
                                val diffText = when {
                                    diffDays == 0L -> "与云端一致"
                                    diffDays > 0L -> "比云端推迟 ${TermStartChangeAlert.formatDaysDiff(diffDays, false)}"
                                    else -> "比云端提前 ${TermStartChangeAlert.formatDaysDiff(-diffDays, false)}"
                                }
                                append("云端最新：${serverTermStartDate.format(dateFormatter)}（$diffText）")
                            } else {
                                append("${currentTermStartTime.data.format(dateFormatter)}（从云端自动获取）")
                            }
                        }
                        Text(text)
                    },
                    onClick = { termStartDialog.show() }
                )
                XhuSettingsMenuLink(
                    icon = {
                        Icon(
                            painter = XhuIcons.userCampus,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface,
                        )
                    },
                    title = { Text("当前主用户校区") },
                    subtitle = { Text("当前：${campusInfo.selected.ifBlank { "加载中…" }}") },
                    onClick = {
                        if (campusInfo != CampusInfo.EMPTY) campusDialog.show()
                    }
                )
            }

            XhuSettingsGroup(title = { Text("个性化与数据管理") }) {
                XhuSettingsMenuLink(
                    icon = {
                        Icon(
                            painter = XhuIcons.customCourseColor,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface,
                        )
                    },
                    title = { Text("自定义课程颜色") },
                    subtitle = { Text("为单门课程指定专属主题色") },
                    onClick = { navController.navigateAndSave(RouteCustomCourseColor) }
                )
                XhuActionSettingsCheckbox(
                    icon = {
                        Icon(
                            painter = XhuIcons.customCourse,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface,
                        )
                    },
                    title = { Text("自定义课程") },
                    subtitle = {
                        Text(if (showCustomCourse) "已开启在课表中展示 · 点击管理" else "已隐藏 · 点击管理")
                    },
                    onCheckedChange = { viewModel.updateShowCustomCourse(it) },
                    checked = showCustomCourse,
                    onClick = { navController.navigateAndSave(RouteCustomCourse) }
                )
                XhuActionSettingsCheckbox(
                    icon = {
                        Icon(
                            painter = XhuIcons.customThing,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface,
                        )
                    },
                    title = { Text("自定义事项") },
                    subtitle = {
                        Text(if (showCustomThing) "已开启在今日页展示 · 点击管理" else "已隐藏 · 点击管理")
                    },
                    onCheckedChange = { viewModel.updateShowCustomThing(it) },
                    checked = showCustomThing,
                    onClick = { navController.navigateAndSave(RouteCustomThing) }
                )
            }

            XhuSettingsGroup(title = { Text("实用工具") }) {
                XhuSettingsMenuLink(
                    icon = {
                        Icon(
                            painter = XhuIcons.schoolCalendar,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface,
                        )
                    },
                    title = { Text("查看校历") },
                    onClick = { navController.navigateAndSave(RouteSchoolCalendar) }
                )
                ExportToCalendarSettings()
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
                    title = { Text("显示节假日信息") },
                    subtitle = { Text("在今日课程顶部提示法定节假日与调休安排") },
                ) {
                    EventBus.post(EventType.CHANGE_SHOW_HOLIDAY)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // 自研时间选择对话框
    XhuTimePickerDialog(
        state = tomorrowTimeDialog,
        title = "自动切换明日课表时间",
        initialTime = showTomorrowCourseTime ?: LocalTime(21, 0),
        extraButtonText = if (showTomorrowCourseTime != null) "关闭功能" else null,
        onExtraButtonClick = { viewModel.updateShowTomorrowCourseTime(null) },
        onSelectTime = { viewModel.updateShowTomorrowCourseTime(it) },
    )

    // 自研学年学期单选对话框
    val currentString = "${currentYear.data}-${currentYear.data + 1}学年 第${currentTerm.data}学期"
    val selectedYearTermIndex = if (currentYear.custom || currentTerm.custom) {
        selectList.indexOf(currentString)
    } else 0
    XhuSingleSelectDialog(
        state = yearTermDialog,
        title = "更改当前学期",
        options = selectList,
        selectedIndex = selectedYearTermIndex,
        onSelect = { index, _ ->
            if (index == 0) {
                viewModel.updateCurrentYearTerm(false, 0, 0)
            } else {
                val select = selectList[index]
                val year = select.substring(0, 4).toInt()
                val term = select.substring(13, 14).toInt()
                viewModel.updateCurrentYearTerm(true, year, term)
            }
        },
    )

    // 自研开学日期选择对话框
    XhuDatePickerDialog(
        state = termStartDialog,
        title = "更改开学时间",
        initialDate = currentTermStartTime.data,
        extraButtonText = "自动获取",
        onExtraButtonClick = {
            viewModel.updateTermStartTime(false, LocalDate.MIN)
        },
        onSelectDate = { viewModel.updateTermStartTime(true, it) },
    )

    // 自研校区单选对话框
    if (campusInfo != CampusInfo.EMPTY) {
        XhuSingleSelectDialog(
            state = campusDialog,
            title = "切换校区",
            options = campusInfo.items,
            selectedIndex = campusInfo.items.indexOf(campusInfo.selected),
            onSelect = { _, campus ->
                viewModel.updateUserCampus(campus)
                showToast("当前校区已更新")
            },
        )
    }

    HandleErrorMessage(flow = viewModel.errorMessage)
}

@Composable
expect fun ExportToCalendarSettings()
