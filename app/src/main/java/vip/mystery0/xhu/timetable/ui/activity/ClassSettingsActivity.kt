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
import vip.mystery0.xhu.timetable.utils.timeFormatter
import vip.mystery0.xhu.timetable.viewmodel.ClassSettingsViewModel
import java.time.LocalTime

class ClassSettingsActivity : BaseComposeActivity(), KoinComponent {
    private val viewModel: ClassSettingsViewModel by viewModels()

    @Composable
    override fun BuildContent() {
        val scope = rememberCoroutineScope()
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
                    val currentYear by viewModel.currentYearData.collectAsState()
                    val currentTerm by viewModel.currentTermData.collectAsState()
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
                            scope.launch {
                                val selectArray = viewModel.buildSelect()
                                val currentString = "${currentYear.first}学年 第${currentTerm.first}学期"
                                var selectedIndex =
                                    if (currentYear.second || currentTerm.second)
                                        selectArray.indexOf(currentString)
                                    else
                                        0
                                androidx.appcompat.app.AlertDialog.Builder(this@ClassSettingsActivity)
                                    .setTitle("更改当前学期")
                                    .setSingleChoiceItems(
                                        selectArray,
                                        selectedIndex
                                    ) { _, checkItem ->
                                        selectedIndex = checkItem
                                    }
                                    .setPositiveButton("确定") { _, _ ->
                                        if (selectedIndex == 0) {
                                            viewModel.updateCurrentYearTerm()
                                        } else {
                                            val select = selectArray[selectedIndex]
                                            val year = select.substring(0, 9)
                                            val term = select.substring(13, 14)
                                            viewModel.updateCurrentYearTerm(year, term.toInt())
                                        }
                                    }
                                    .setNegativeButton("取消", null)
                                    .show()
                            }
                        }
                    )
                    val currentTermStartTime by viewModel.currentTermStartTime.collectAsState()
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
                                append(currentTermStartTime.first)
                                if (!currentTermStartTime.second) {
                                    appendLine()
                                    append("【从云端自动获取】")
                                }
                            }
                            Text(text = text)
                        },
                        onClick = {
                            scope.launch {
                                "暂未实现".toast()
                            }
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
                            scope.launch {
                                "暂未实现".toast()
                            }
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
        val errorMessage by viewModel.errorMessage.collectAsState()
        if (errorMessage.isNotBlank()) {
            errorMessage.toast(true)
        }
    }
}