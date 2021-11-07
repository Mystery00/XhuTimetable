package vip.mystery0.xhu.timetable.ui.activity

import android.app.TimePickerDialog
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import com.alorma.compose.settings.ui.SettingsMenuLink
import com.vanpra.composematerialdialogs.*
import com.vanpra.composematerialdialogs.datetime.date.datepicker
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import vip.mystery0.xhu.timetable.base.BaseComposeActivity
import vip.mystery0.xhu.timetable.config.GlobalConfig
import vip.mystery0.xhu.timetable.config.setConfig
import vip.mystery0.xhu.timetable.model.event.EventType
import vip.mystery0.xhu.timetable.model.event.UIEvent
import vip.mystery0.xhu.timetable.ui.preference.ConfigSettingsCheckbox
import vip.mystery0.xhu.timetable.ui.preference.ConfigSettingsMenuLink
import vip.mystery0.xhu.timetable.ui.preference.XhuSettingsGroup
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
        val currentTermStartTime by viewModel.currentTermStartTime.collectAsState()
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
                        config = GlobalConfig::showNotThisWeek,
                        scope = scope,
                        icon = {
                            Icon(
                                painter = XhuIcons.showNotThisWeek,
                                contentDescription = null,
                                tint = XhuColor.Common.blackText,
                            )
                        },
                        title = { Text(text = "显示非本周课程") },
                        onCheckedChange = {
                            setConfig { showNotThisWeek = it }
                            eventBus.post(UIEvent(EventType.CHANGE_SHOW_NOT_THIS_WEEK))
                        }
                    )
                    ConfigSettingsCheckbox(
                        config = GlobalConfig::showStatus,
                        scope = scope,
                        icon = {
                            Icon(
                                painter = XhuIcons.showCourseStatus,
                                contentDescription = null,
                                tint = XhuColor.Common.blackText,
                            )
                        },
                        title = { Text(text = "显示今日课程状态") },
                        onCheckedChange = {
                            setConfig { showStatus = it }
                            eventBus.post(UIEvent(EventType.CHANGE_SHOW_STATUS))
                        }
                    )
                    ConfigSettingsMenuLink(
                        config = GlobalConfig::showTomorrowCourseTime,
                        scope = scope,
                        title = { Text(text = "自动显示明日课程") },
                        subtitle = { value ->
                            Text(
                                text = if (value != null)
                                    "在每天的 ${value.format(timeFormatter)} 后今日课程页面显示明日的课表"
                                else
                                    "此功能已禁用，今日课程页面只会显示今日的课表"
                            )
                        },
                        action = { value, setter ->
                            Checkbox(
                                checked = value != null,
                                onCheckedChange = {
                                    setter(null)
                                    eventBus.post(UIEvent(EventType.CHANGE_AUTO_SHOW_TOMORROW_COURSE))
                                },
                                enabled = value != null,
                            )
                        },
                        onClick = { value, setter ->
                            val time = value ?: LocalTime.now()
                            TimePickerDialog(
                                this@ClassSettingsActivity,
                                { _, hourOfDay, minute ->
                                    scope.launch {
                                        val newTime = LocalTime.of(hourOfDay, minute, 0)
                                        setter(newTime)
                                        eventBus.post(UIEvent(EventType.CHANGE_AUTO_SHOW_TOMORROW_COURSE))
                                    }
                                },
                                time.hour,
                                time.minute,
                                true
                            ).show()
                        }
                    )
                }
                XhuSettingsGroup(title = {
                    Text(text = "时间设置")
                }) {
                    SettingsMenuLink(
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
                                append("${currentYear.first}学年")
                                append(" ")
                                append("第${currentTerm.first}学期")
                                if (!currentYear.second && !currentTerm.second) {
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
                    SettingsMenuLink(
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
                                append(currentTermStartTime.first.format(dateFormatter))
                                if (!currentTermStartTime.second) {
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
                    SettingsMenuLink(
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
                    SettingsMenuLink(
                        icon = {
                            Icon(
                                painter = XhuIcons.schoolCalendar,
                                contentDescription = null,
                                tint = XhuColor.Common.blackText,
                            )
                        },
                        title = { Text(text = "查看校历") },
                        onClick = {
                            scope.launch {
                                "暂未实现".toast()
                            }
                        }
                    )
                }
            }
        }
        val selectList by viewModel.selectYearAndTermList.collectAsState()
        BuildYearAndTermSelector(
            dialogState = yearAndTermState,
            selectList = selectList,
            currentYear = currentYear,
            currentTerm = currentTerm,
        )
        BuildTermStartTimeSelector(
            dialogState = termStartTimeState,
            initDate = currentTermStartTime.first,
        )
        val errorMessage by viewModel.errorMessage.collectAsState()
        if (errorMessage.isNotBlank()) {
            errorMessage.toast(true)
        }
    }

    @Composable
    private fun BuildYearAndTermSelector(
        dialogState: MaterialDialogState,
        selectList: List<String>,
        currentYear: Pair<String, Boolean>,
        currentTerm: Pair<Int, Boolean>,
    ) {
        val currentString = "${currentYear.first}学年 第${currentTerm.first}学期"
        var selectedIndex =
            if (currentYear.second || currentTerm.second)
                selectList.indexOf(currentString)
            else
                0
        MaterialDialog(
            dialogState = dialogState,
            buttons = {
                positiveButton("确定") {
                    if (selectedIndex == 0) {
                        viewModel.updateCurrentYearTerm()
                    } else {
                        val select = selectList[selectedIndex]
                        val year = select.substring(0, 9)
                        val term = select.substring(13, 14)
                        viewModel.updateCurrentYearTerm(year, term.toInt())
                    }
                }
                negativeButton("取消")
            }) {
            title("更改当前学期")
            listItemsSingleChoice(
                list = selectList,
                initialSelection = selectedIndex
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
                    viewModel.updateTermStartTime(selectedDate)
                }
                negativeButton("自动获取") {
                    viewModel.updateTermStartTime(LocalDate.MIN)
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