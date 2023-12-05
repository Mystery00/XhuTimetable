package vip.mystery0.xhu.timetable.ui.activity

import androidx.activity.viewModels
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import com.maxkeppeker.sheets.core.models.base.Header
import com.maxkeppeker.sheets.core.models.base.SelectionButton
import com.maxkeppeker.sheets.core.models.base.rememberUseCaseState
import com.maxkeppeler.sheets.calendar.CalendarDialog
import com.maxkeppeler.sheets.calendar.models.CalendarConfig
import com.maxkeppeler.sheets.calendar.models.CalendarSelection
import com.maxkeppeler.sheets.calendar.models.CalendarStyle
import com.maxkeppeler.sheets.date_time.DateTimeDialog
import com.maxkeppeler.sheets.date_time.models.DateTimeSelection
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import vip.mystery0.xhu.timetable.base.BaseSelectComposeActivity
import vip.mystery0.xhu.timetable.config.Customisable
import vip.mystery0.xhu.timetable.config.store.ConfigStore
import vip.mystery0.xhu.timetable.config.store.EventBus
import vip.mystery0.xhu.timetable.model.event.EventType
import vip.mystery0.xhu.timetable.ui.component.XhuDialogState
import vip.mystery0.xhu.timetable.ui.component.rememberXhuDialogState
import vip.mystery0.xhu.timetable.ui.preference.ConfigSettingsCheckbox
import vip.mystery0.xhu.timetable.ui.preference.XhuActionSettingsCheckbox
import vip.mystery0.xhu.timetable.ui.preference.XhuSettingsGroup
import vip.mystery0.xhu.timetable.ui.preference.XhuSettingsMenuLink
import vip.mystery0.xhu.timetable.ui.theme.XhuIcons
import vip.mystery0.xhu.timetable.utils.dateFormatter
import vip.mystery0.xhu.timetable.utils.timeFormatter
import vip.mystery0.xhu.timetable.viewmodel.ClassSettingsViewModel
import java.time.LocalDate
import java.time.LocalTime

class ClassSettingsActivity : BaseSelectComposeActivity(), KoinComponent {
    private val viewModel: ClassSettingsViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun BuildContent() {
        val scope = rememberCoroutineScope()
        val currentYear by viewModel.currentYearData.collectAsState()
        val currentTerm by viewModel.currentTermData.collectAsState()
        val showTomorrowCourseTime by viewModel.showTomorrowCourseTimeData.collectAsState()
        val currentTermStartTime by viewModel.currentTermStartTime.collectAsState()
        val showCustomCourse by viewModel.showCustomCourseData.collectAsState()
        val showCustomThing by viewModel.showCustomThingData.collectAsState()
        val showTomorrowCourseTimeState = rememberXhuDialogState()
        val yearAndTermState = rememberXhuDialogState()
        val termStartTimeState = rememberXhuDialogState()
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(text = title.toString()) },
                    navigationIcon = {
                        IconButton(onClick = {
                            finish()
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
                            intentTo(CustomCourseColorActivity::class)
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
                            intentTo(SchoolCalendarActivity::class)
                        }
                    )
                    XhuSettingsMenuLink(
                        icon = {
                            Icon(
                                painter = XhuIcons.exportCalendar,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurface,
                            )
                        },
                        title = { Text(text = "导出到日历") },
                        onClick = {
                            intentTo(ExportCalendarActivity::class)
                        }
                    )
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
                            scope.launch {
                                intentTo(CustomCourseActivity::class)
                            }
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
                            scope.launch {
                                intentTo(CustomThingActivity::class)
                            }
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
            dialogState = showTomorrowCourseTimeState,
            initTime = showTomorrowCourseTime ?: LocalTime.now(),
        )
        val selectList by viewModel.selectYearAndTermList.collectAsState()
        BuildYearAndTermSelector(
            dialogState = yearAndTermState,
            selectList = selectList,
            currentYear = currentYear,
            currentTerm = currentTerm,
        )
        BuildTermStartTimeSelector(
            dialogState = termStartTimeState,
            initDate = currentTermStartTime.data,
        )
        val errorMessage by viewModel.errorMessage.collectAsState()
        if (errorMessage.isNotBlank()) {
            errorMessage.toast(true)
        }
    }

    @Composable
    private fun BuildTimeSelector(
        dialogState: XhuDialogState,
        initTime: LocalTime,
    ) {
        if (dialogState.showing) {
            DateTimeDialog(
                header = Header.Default(
                    title = "请选择时间",
                ),
                state = rememberUseCaseState(
                    visible = true,
                    onCloseRequest = {
                        dialogState.hide()
                    }),
                selection = DateTimeSelection.Time(
                    selectedTime = initTime,
                ) { newTime ->
                    viewModel.updateShowTomorrowCourseTime(newTime)
                },
            )
        }
    }

    @Composable
    private fun BuildYearAndTermSelector(
        dialogState: XhuDialogState,
        selectList: List<String>,
        currentYear: Customisable<Int>,
        currentTerm: Customisable<Int>,
    ) {
        val currentString =
            "${currentYear.data}-${currentYear.data + 1}学年 第${currentTerm.data}学期"
        val selectedIndex =
            if (currentYear.custom || currentTerm.custom)
                selectList.indexOf(currentString)
            else
                0

        ShowSelectDialog(
            dialogTitle = "更改当前学期",
            options = selectList,
            selectIndex = selectedIndex,
            state = dialogState,
            onSelect = { index, _ ->
                if (index == 0) {
                    viewModel.updateCurrentYearTerm(custom = false)
                } else {
                    val select = selectList[index]
                    val year = select.substring(0, 4)
                    val term = select.substring(13, 14)
                    viewModel.updateCurrentYearTerm(custom = true, year.toInt(), term.toInt())
                }
            }
        )
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun BuildTermStartTimeSelector(
        dialogState: XhuDialogState,
        initDate: LocalDate,
    ) {
        if (dialogState.showing) {
            CalendarDialog(
                header = Header.Default(
                    title = "更改开学时间",
                ),
                state = rememberUseCaseState(
                    visible = true,
                    onCloseRequest = {
                        dialogState.hide()
                    }),
                selection = CalendarSelection.Date(
                    selectedDate = initDate,
                    extraButton = SelectionButton(text = "自动获取"),
                    onExtraButtonClick = {
                        viewModel.updateTermStartTime(false, LocalDate.MIN)
                        dialogState.hide()
                    }
                ) {
                    viewModel.updateTermStartTime(true, it)
                },
                config = CalendarConfig(
                    yearSelection = true,
                    monthSelection = true,
                    style = CalendarStyle.MONTH,
                    boundary = LocalDate.of(2015, 1, 1)..LocalDate.now().plusYears(1),
                )
            )
        }
    }
}