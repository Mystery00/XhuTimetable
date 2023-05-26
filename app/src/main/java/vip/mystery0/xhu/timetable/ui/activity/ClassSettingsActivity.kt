package vip.mystery0.xhu.timetable.ui.activity

import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Checkbox
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import com.vanpra.composematerialdialogs.MaterialDialog
import com.vanpra.composematerialdialogs.MaterialDialogState
import com.vanpra.composematerialdialogs.datetime.date.datepicker
import com.vanpra.composematerialdialogs.datetime.time.timepicker
import com.vanpra.composematerialdialogs.listItemsSingleChoice
import com.vanpra.composematerialdialogs.rememberMaterialDialogState
import com.vanpra.composematerialdialogs.title
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import vip.mystery0.xhu.timetable.base.BaseComposeActivity
import vip.mystery0.xhu.timetable.config.Customisable
import vip.mystery0.xhu.timetable.config.store.ConfigStore
import vip.mystery0.xhu.timetable.model.event.EventType
import vip.mystery0.xhu.timetable.model.event.UIEvent
import vip.mystery0.xhu.timetable.ui.preference.ConfigSettingsCheckbox
import vip.mystery0.xhu.timetable.ui.preference.XhuSettingsGroup
import vip.mystery0.xhu.timetable.ui.preference.XhuSettingsMenuLink
import vip.mystery0.xhu.timetable.ui.theme.XhuColor
import vip.mystery0.xhu.timetable.ui.theme.XhuIcons
import vip.mystery0.xhu.timetable.utils.dateFormatter
import vip.mystery0.xhu.timetable.utils.timeFormatter
import vip.mystery0.xhu.timetable.viewmodel.ClassSettingsViewModel
import java.time.LocalDate
import java.time.LocalTime

class ClassSettingsActivity : BaseComposeActivity(), KoinComponent {
    private val viewModel: ClassSettingsViewModel by viewModels()

    @Composable
    override fun BuildContent() {
        val scope = rememberCoroutineScope()
        val currentYear by viewModel.currentYearData.collectAsState()
        val currentTerm by viewModel.currentTermData.collectAsState()
        val showTomorrowCourseTime by viewModel.showTomorrowCourseTimeData.collectAsState()
        val currentTermStartTime by viewModel.currentTermStartTime.collectAsState()
        val showCustomCourse by viewModel.showCustomCourseData.collectAsState()
        val showCustomThing by viewModel.showCustomThingData.collectAsState()
        val showTomorrowCourseTimeState = rememberMaterialDialogState()
        val yearAndTermState = rememberMaterialDialogState()
        val termStartTimeState = rememberMaterialDialogState()
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(text = title.toString()) },
                    backgroundColor = MaterialTheme.colors.primary,
                    contentColor = MaterialTheme.colors.onPrimary,
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
                    .background(XhuColor.Common.grayBackground)
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
                                tint = XhuColor.Common.blackText,
                            )
                        },
                        title = { Text(text = "显示非本周课程") }
                    ) {
                        eventBus.post(UIEvent(EventType.CHANGE_SHOW_NOT_THIS_WEEK))
                    }
                    ConfigSettingsCheckbox(
                        config = ConfigStore::showStatus,
                        scope = scope,
                        icon = {
                            Icon(
                                painter = XhuIcons.showCourseStatus,
                                contentDescription = null,
                                tint = XhuColor.Common.blackText,
                            )
                        },
                        title = { Text(text = "显示今日课程状态") }
                    ) {
                        eventBus.post(UIEvent(EventType.CHANGE_SHOW_STATUS))
                    }
                    XhuSettingsMenuLink(
                        title = { Text(text = "自动显示明日课程") },
                        subtitle = {
                            Text(
                                text = if (showTomorrowCourseTime != null)
                                    "在每天的 ${showTomorrowCourseTime!!.format(timeFormatter)} 后今日课程页面显示明日的课表"
                                else
                                    "此功能已禁用，今日课程页面只会显示今日的课表"
                            )
                        },
                        action = {
                            Checkbox(
                                checked = showTomorrowCourseTime != null,
                                onCheckedChange = {
                                    viewModel.updateShowTomorrowCourseTime(null)
                                },
                                enabled = showTomorrowCourseTime != null,
                            )
                        },
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
                                tint = XhuColor.Common.blackText,
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
                                tint = XhuColor.Common.blackText,
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
                                tint = XhuColor.Common.blackText,
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
                                tint = XhuColor.Common.blackText,
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
                                tint = XhuColor.Common.blackText,
                            )
                        },
                        title = { Text(text = "导出到日历") },
                        onClick = {
                            intentTo(ExportCalendarActivity::class)
                        }
                    )
                    XhuSettingsMenuLink(
                        icon = {
                            Icon(
                                painter = XhuIcons.customCourse,
                                contentDescription = null,
                                tint = XhuColor.Common.blackText,
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
                        action = {
                            Checkbox(
                                checked = showCustomCourse,
                                onCheckedChange = {
                                    viewModel.updateShowCustomCourse(it)
                                },
                            )
                        },
                        onClick = {
                            intentTo(CustomCourseActivity::class)
                        }
                    )
                    XhuSettingsMenuLink(
                        icon = {
                            Icon(
                                painter = XhuIcons.customThing,
                                contentDescription = null,
                                tint = XhuColor.Common.blackText,
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
                        action = {
                            Checkbox(
                                checked = showCustomThing,
                                onCheckedChange = {
                                    viewModel.updateShowCustomThing(it)
                                },
                            )
                        },
                        onClick = {
                            scope.launch {
                                intentTo(CustomThingActivity::class)
                            }
                        }
                    )
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
        dialogState: MaterialDialogState,
        initTime: LocalTime,
    ) {
        var selectedTime = initTime
        MaterialDialog(
            dialogState = dialogState,
            buttons = {
                positiveButton("确定") {
                    viewModel.updateShowTomorrowCourseTime(selectedTime)
                }
                negativeButton("取消")
            }) {
            timepicker(
                title = "请选择时间",
                initialTime = selectedTime,
                is24HourClock = true,
            ) {
                selectedTime = it
            }
        }
    }

    @Composable
    private fun BuildYearAndTermSelector(
        dialogState: MaterialDialogState,
        selectList: List<String>,
        currentYear: Customisable<Int>,
        currentTerm: Customisable<Int>,
    ) {
        val currentString =
            "${currentYear.data}-${currentYear.data + 1}学年 第${currentTerm.data}学期"
        var selectedIndex =
            if (currentYear.custom || currentTerm.custom)
                selectList.indexOf(currentString)
            else
                0
        MaterialDialog(
            dialogState = dialogState,
            buttons = {
                positiveButton("确定") {
                    if (selectedIndex == 0) {
                        viewModel.updateCurrentYearTerm(custom = false)
                    } else {
                        val select = selectList[selectedIndex]
                        val year = select.substring(0, 4)
                        val term = select.substring(13, 14)
                        viewModel.updateCurrentYearTerm(custom = true, year.toInt(), term.toInt())
                    }
                }
                negativeButton("取消")
            }) {
            title("更改当前学期")
            listItemsSingleChoice(
                list = selectList,
                initialSelection = selectedIndex,
            ) {
                selectedIndex = it
            }
        }
    }

    @Composable
    private fun BuildTermStartTimeSelector(
        dialogState: MaterialDialogState,
        initDate: LocalDate,
    ) {
        var selectedDate = initDate
        MaterialDialog(
            dialogState = dialogState,
            buttons = {
                positiveButton("确定") {
                    viewModel.updateTermStartTime(true, selectedDate)
                }
                negativeButton("自动获取") {
                    viewModel.updateTermStartTime(false, LocalDate.MIN)
                }
            }) {
            datepicker(
                title = "更改开学时间",
                initialDate = initDate,
                yearRange = 2015..LocalDate.now().year
            ) {
                selectedDate = it
            }
        }
    }
}