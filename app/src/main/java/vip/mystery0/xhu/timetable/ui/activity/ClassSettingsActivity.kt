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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.koin.core.component.KoinComponent
import vip.mystery0.xhu.timetable.base.BaseComposeActivity
import vip.mystery0.xhu.timetable.config.Config
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
                    config = Config::showNotThisWeek,
                    icon = { Icon(painter = XhuIcons.showNotThisWeek, contentDescription = null) },
                    title = { Text(text = "显示非本周课程") },
                    subtitle = { },
                )
                ConfigSettingsMenuLink(
                    config = Config::showTomorrowCourseTime,
                    icon = {},
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
                                val newTime = LocalTime.of(hourOfDay, minute, 0)
                                setter(newTime)
//                                eventBus.post(UIConfigEvent(arrayListOf(UI.MAIN_INIT, UI.MENU)))
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