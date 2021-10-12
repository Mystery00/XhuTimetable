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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.alorma.settings.composables.SettingsMenuLink
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import vip.mystery0.xhu.timetable.base.BaseComposeActivity
import vip.mystery0.xhu.timetable.config.GlobalConfig
import vip.mystery0.xhu.timetable.config.rememberConfigState
import vip.mystery0.xhu.timetable.config.setConfig
import vip.mystery0.xhu.timetable.model.event.EventType
import vip.mystery0.xhu.timetable.model.event.UIEvent
import vip.mystery0.xhu.timetable.ui.preference.ConfigSettingsCheckbox
import vip.mystery0.xhu.timetable.ui.preference.ConfigSettingsMenuLink
import vip.mystery0.xhu.timetable.ui.theme.XhuColor
import vip.mystery0.xhu.timetable.ui.theme.XhuIcons
import vip.mystery0.xhu.timetable.utils.timeFormatter
import vip.mystery0.xhu.timetable.viewmodel.ClassSettingsViewModel
import java.time.LocalTime
import java.util.*

class ClassSettingsActivity : BaseComposeActivity(), KoinComponent {

    private val viewModel: ClassSettingsViewModel by viewModels()

    @Composable
    override fun BuildContent() {
        val scope = rememberCoroutineScope()
        val errorMessage by viewModel.errorMessage.collectAsState()
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
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                ConfigSettingsCheckbox(
                    config = GlobalConfig::showNotThisWeek,
                    scope = scope,
                    icon = { Icon(painter = XhuIcons.showNotThisWeek, contentDescription = null) },
                    title = { Text(text = "显示非本周课程") },
                    onCheckedChange = {
                        setConfig { showNotThisWeek = it }
                        eventBus.post(UIEvent(EventType.CHANGE_SHOW_NOT_THIS_WEEK))
                    }
                )
                var currentYear by rememberConfigState(property = GlobalConfig::currentYearData)
                var currentTerm by rememberConfigState(property = GlobalConfig::currentTermData)
                SettingsMenuLink(
                    title = { Text(text = "更改当前学期") },
                    subtitle = {
                        Text(text = "当前学期：${currentYear.first}学年 第${currentTerm.first}学期")
                    },
                    onClick = {
                        scope.launch {
                            val range = viewModel.buildSelect()
                            val tempArrayList = ArrayList<String>()
                            range.forEach {
                                tempArrayList.add("${it}-${it + 1}学年 第1学期")
                                tempArrayList.add("${it}-${it + 1}学年 第2学期")
                            }
                            tempArrayList.sortDescending()
                            tempArrayList.add(0, "自动获取")
                            val selectArray = tempArrayList.toTypedArray()
                            val currentString = "${currentYear.first}学年 第${currentTerm.first}学期"
                            var selectedIndex =
                                if (currentYear.second || currentTerm.second)
                                    selectArray.indexOf(currentString)
                                else
                                    0
                            MaterialAlertDialogBuilder(this@ClassSettingsActivity)
                                .setTitle("")
                                .setSingleChoiceItems(selectArray, selectedIndex) { _, checkItem ->
                                    selectedIndex = checkItem
                                }
                                .setPositiveButton("确定") { _, _ ->
                                    if (selectedIndex == 0) {
                                        currentYear = "" to false
                                    } else {
                                        val select = selectArray[selectedIndex]
                                        val year = select.substring(0, 9)
                                        val term = select.substring(13, 14)
                                        currentYear = year to true
                                        currentTerm = term.toInt() to true
                                    }
                                    eventBus.post(UIEvent(EventType.CHANGE_CURRENT_YEAR_AND_TERM))
                                }
                                .setNegativeButton("取消", null)
                                .show()
                        }
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
        }
        if (errorMessage.isNotBlank()) {
            errorMessage.toast(true)
        }
    }
}