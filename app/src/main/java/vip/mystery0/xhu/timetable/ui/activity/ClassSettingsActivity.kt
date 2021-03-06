package vip.mystery0.xhu.timetable.ui.activity

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
import com.vanpra.composematerialdialogs.datetime.time.timepicker
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import vip.mystery0.xhu.timetable.base.BaseComposeActivity
import vip.mystery0.xhu.timetable.config.GlobalConfig
import vip.mystery0.xhu.timetable.config.setConfig
import vip.mystery0.xhu.timetable.model.event.EventType
import vip.mystery0.xhu.timetable.model.event.UIEvent
import vip.mystery0.xhu.timetable.ui.preference.ConfigSettingsCheckbox
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
                    Text(text = "????????????")
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
                        title = { Text(text = "?????????????????????") },
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
                        title = { Text(text = "????????????????????????") },
                        onCheckedChange = {
                            setConfig { showStatus = it }
                            eventBus.post(UIEvent(EventType.CHANGE_SHOW_STATUS))
                        }
                    )
                    SettingsMenuLink(
                        title = { Text(text = "????????????????????????") },
                        subtitle = {
                            Text(
                                text = if (showTomorrowCourseTime != null)
                                    "???????????? ${showTomorrowCourseTime!!.format(timeFormatter)} ??????????????????????????????????????????"
                                else
                                    "??????????????????????????????????????????????????????????????????"
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
                    Text(text = "????????????")
                }) {
                    SettingsMenuLink(
                        icon = {
                            Icon(
                                painter = XhuIcons.customYearTerm,
                                contentDescription = null,
                                tint = XhuColor.Common.blackText,
                            )
                        },
                        title = { Text(text = "??????????????????") },
                        subtitle = {
                            val text = buildString {
                                append("???????????????")
                                append("${currentYear.first}??????")
                                append(" ")
                                append("???${currentTerm.first}??????")
                                if (!currentYear.second && !currentTerm.second) {
                                    appendLine()
                                    append("??????????????????????????????????????????")
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
                        title = { Text(text = "??????????????????") },
                        subtitle = {
                            val text = buildString {
                                append("?????????????????????")
                                append(currentTermStartTime.first.format(dateFormatter))
                                if (!currentTermStartTime.second) {
                                    appendLine()
                                    append("???????????????????????????")
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
                    Text(text = "???????????????")
                }) {
                    SettingsMenuLink(
                        icon = {
                            Icon(
                                painter = XhuIcons.customCourseColor,
                                contentDescription = null,
                                tint = XhuColor.Common.blackText,
                            )
                        },
                        title = { Text(text = "?????????????????????") },
                        onClick = {
                            intentTo(CustomCourseColorActivity::class)
                        }
                    )
                }
                XhuSettingsGroup(title = {
                    Text(text = "????????????")
                }) {
                    ConfigSettingsCheckbox(
                        config = GlobalConfig::autoCacheJwcCourse,
                        scope = scope,
                        title = { Text(text = "??????????????????????????????") },
                        subtitle = {
                            Text(text = "????????????????????????????????????????????????????????????????????????APP??????????????????????????????")
                        },
                        onCheckedChange = {
                            setConfig { autoCacheJwcCourse = it }
                        }
                    )
                }
                XhuSettingsGroup(title = {
                    Text(text = "????????????")
                }) {
                    SettingsMenuLink(
                        icon = {
                            Icon(
                                painter = XhuIcons.schoolCalendar,
                                contentDescription = null,
                                tint = XhuColor.Common.blackText,
                            )
                        },
                        title = { Text(text = "????????????") },
                        onClick = {
                            intentTo(SchoolCalendarActivity::class)
                        }
                    )
                    SettingsMenuLink(
                        icon = {
                            Icon(
                                painter = XhuIcons.exportCalendar,
                                contentDescription = null,
                                tint = XhuColor.Common.blackText,
                            )
                        },
                        title = { Text(text = "???????????????") },
                        onClick = {
                            intentTo(ExportCalendarActivity::class)
                        }
                    )
                    SettingsMenuLink(
                        icon = {
                            Icon(
                                painter = XhuIcons.customCourse,
                                contentDescription = null,
                                tint = XhuColor.Common.blackText,
                            )
                        },
                        title = { Text(text = "???????????????") },
                        subtitle = {
                            val text = if (showCustomCourse)
                                "???????????????????????????????????????????????????"
                            else
                                "???????????????????????????????????????"
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
                    SettingsMenuLink(
                        icon = {
                            Icon(
                                painter = XhuIcons.customThing,
                                contentDescription = null,
                                tint = XhuColor.Common.blackText,
                            )
                        },
                        title = { Text(text = "???????????????") },
                        subtitle = {
                            val text = if (showCustomThing)
                                "????????????????????????????????????????????????????????????"
                            else
                                "???????????????????????????????????????"
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
            initDate = currentTermStartTime.first,
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
                positiveButton("??????") {
                    viewModel.updateShowTomorrowCourseTime(selectedTime)
                }
                negativeButton("??????")
            }) {
            timepicker(
                title = "???????????????",
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
        currentYear: Pair<String, Boolean>,
        currentTerm: Pair<Int, Boolean>,
    ) {
        val currentString = "${currentYear.first}?????? ???${currentTerm.first}??????"
        var selectedIndex =
            if (currentYear.second || currentTerm.second)
                selectList.indexOf(currentString)
            else
                0
        MaterialDialog(
            dialogState = dialogState,
            buttons = {
                positiveButton("??????") {
                    if (selectedIndex == 0) {
                        viewModel.updateCurrentYearTerm()
                    } else {
                        val select = selectList[selectedIndex]
                        val year = select.substring(0, 9)
                        val term = select.substring(13, 14)
                        viewModel.updateCurrentYearTerm(year, term.toInt())
                    }
                }
                negativeButton("??????")
            }) {
            title("??????????????????")
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
                positiveButton("??????") {
                    viewModel.updateTermStartTime(selectedDate)
                }
                negativeButton("????????????") {
                    viewModel.updateTermStartTime(LocalDate.MIN)
                }
            }) {
            datepicker(
                title = "??????????????????",
                initialDate = initDate,
                yearRange = 2015..LocalDate.now().year
            ) {
                selectedDate = it
            }
        }
    }
}