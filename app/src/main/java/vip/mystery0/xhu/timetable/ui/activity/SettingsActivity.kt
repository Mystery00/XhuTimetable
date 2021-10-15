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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import com.alorma.settings.composables.SettingsMenuLink
import kotlinx.coroutines.launch
import vip.mystery0.xhu.timetable.appVersionCode
import vip.mystery0.xhu.timetable.appVersionName
import vip.mystery0.xhu.timetable.base.BaseComposeActivity
import vip.mystery0.xhu.timetable.config.GlobalConfig
import vip.mystery0.xhu.timetable.config.setConfig
import vip.mystery0.xhu.timetable.loadInBrowser
import vip.mystery0.xhu.timetable.model.event.EventType
import vip.mystery0.xhu.timetable.model.event.UIEvent
import vip.mystery0.xhu.timetable.ui.preference.ConfigSettingsCheckbox
import vip.mystery0.xhu.timetable.ui.preference.ConfigSettingsMenuLink
import vip.mystery0.xhu.timetable.ui.preference.XhuSettingsGroup
import vip.mystery0.xhu.timetable.ui.theme.XhuColor
import vip.mystery0.xhu.timetable.ui.theme.XhuIcons
import vip.mystery0.xhu.timetable.utils.timeFormatter
import vip.mystery0.xhu.timetable.viewmodel.SettingsViewModel
import java.time.LocalTime

class SettingsActivity : BaseComposeActivity() {
    private val viewModel: SettingsViewModel by viewModels()

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
                    Text(text = "界面设置")
                }) {
                    SettingsMenuLink(
                        title = { Text(text = "自定义背景图片") },
                        onClick = {
                            scope.launch {
                                "暂未实现".toast()
                            }
                        }
                    )
                    SettingsMenuLink(
                        icon = {
                            Icon(
                                painter = XhuIcons.nightMode,
                                contentDescription = null,
                                tint = XhuColor.Common.blackText,
                            )
                        },
                        title = { Text(text = "夜间模式") },
                        onClick = {
                            scope.launch {
                                "暂未实现".toast()
                            }
                        }
                    )
                    ConfigSettingsCheckbox(
                        config = GlobalConfig::enablePageEffect,
                        scope = scope,
                        icon = {
                            Icon(
                                painter = XhuIcons.pageEffect,
                                contentDescription = null,
                                tint = XhuColor.Common.blackText,
                            )
                        },
                        title = { Text(text = "启用切换动画") },
                        onCheckedChange = {
                            setConfig { enablePageEffect = it }
                            eventBus.post(UIEvent(EventType.CHANGE_PAGE_EFFECT))
                        }
                    )
                }
                XhuSettingsGroup(title = {
                    Text(text = "重置设置")
                }) {
                    SettingsMenuLink(
                        title = { Text(text = "重置背景图片") },
                        onClick = {
                            scope.launch {
                                "暂未实现".toast()
                            }
                        }
                    )
                }
                XhuSettingsGroup(title = {
                    Text(text = "通知设置")
                }) {
                    ConfigSettingsCheckbox(
                        config = GlobalConfig::notifyCourse,
                        scope = scope,
                        icon = {
                            Icon(
                                painter = XhuIcons.notifyCourse,
                                contentDescription = null,
                                tint = XhuColor.Common.blackText,
                            )
                        },
                        title = { Text(text = "课程提醒") },
                        onCheckedChange = {
                            setConfig { notifyCourse = it }
                        }
                    )
                    ConfigSettingsCheckbox(
                        config = GlobalConfig::notifyExam,
                        scope = scope,
                        icon = {
                            Icon(
                                painter = XhuIcons.notifyExam,
                                contentDescription = null,
                                tint = XhuColor.Common.blackText,
                            )
                        },
                        title = { Text(text = "考试提醒") },
                        onCheckedChange = {
                            setConfig { notifyExam = it }
                        }
                    )
                    ConfigSettingsMenuLink(
                        config = GlobalConfig::notifyTime,
                        icon = {
                            Icon(
                                painter = XhuIcons.notifyTime,
                                contentDescription = null,
                                tint = XhuColor.Common.blackText,
                            )
                        },
                        title = { Text(text = "提醒时间") },
                        subtitle = { value ->
                            Text(
                                text = if (value != null)
                                    "将会在每天的 ${value.format(timeFormatter)} 提醒"
                                else
                                    "提醒功能已禁用"
                            )
                        },
                        onClick = { value, setter ->
                            val time = value ?: LocalTime.now()
                            TimePickerDialog(
                                this@SettingsActivity,
                                { _, hourOfDay, minute ->
                                    scope.launch {
                                        val newTime = LocalTime.of(hourOfDay, minute, 0)
                                        setter(newTime)
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
                    Text(text = "诗词设置")
                }) {
                    ConfigSettingsCheckbox(
                        config = GlobalConfig::disablePoems,
                        scope = scope,
                        title = { Text(text = "禁用今日诗词") },
                        onCheckedChange = {
                            setConfig { disablePoems = it }
                        }
                    )
                    ConfigSettingsCheckbox(
                        config = GlobalConfig::showPoemsTranslate,
                        scope = scope,
                        title = { Text(text = "显示诗词大意") },
                        onCheckedChange = {
                            setConfig { showPoemsTranslate = it }
                        }
                    )
                }
                XhuSettingsGroup(title = {
                    Text(text = "其他设置")
                }) {
                    SettingsMenuLink(
                        icon = {
                            Icon(
                                painter = XhuIcons.checkUpdate,
                                contentDescription = null,
                                tint = XhuColor.Common.blackText,
                            )
                        },
                        title = { Text(text = "检查更新") },
                        onClick = {
                            scope.launch {
                                "暂未实现".toast()
                            }
                        }
                    )
                    SettingsMenuLink(
                        icon = {
                            Icon(
                                painter = XhuIcons.qqGroup,
                                contentDescription = null,
                                tint = XhuColor.Common.blackText,
                            )
                        },
                        title = { Text(text = "点击加入『西瓜课表用户交流反馈』") },
                        onClick = {
                            scope.launch {
                                "暂未实现".toast()
                            }
                        }
                    )
                }
                XhuSettingsGroup(title = {
                    Text(text = "应用关于")
                }) {
                    SettingsMenuLink(
                        title = { Text(text = "更新日志") },
                        onClick = {
                            scope.launch {
                                "暂未实现".toast()
                            }
                        }
                    )
                    SettingsMenuLink(
                        title = { Text(text = "开源地址") },
                        subtitle = {
                            Text(text = "https://github.com/Mystery00/XhuTimetable")
                        },
                        onClick = {
                            loadInBrowser("https://github.com/Mystery00/XhuTimetable")
                        }
                    )
                    SettingsMenuLink(
                        icon = {
                            Icon(
                                painter = XhuIcons.poems,
                                contentDescription = null,
                                tint = XhuColor.Common.blackText,
                            )
                        },
                        title = { Text(text = "今日诗词") },
                        subtitle = {
                            Text(text = "感谢作者@xenv，点击访问『今日诗词』官网")
                        },
                        onClick = {
                            loadInBrowser("https://www.jinrishici.com")
                        }
                    )
                }
                XhuSettingsGroup(title = {
                    Text(text = "版本关于")
                }) {
                    SettingsMenuLink(
                        title = { Text(text = "版本名称") },
                        subtitle = {
                            Text(text = appVersionName)
                        },
                        onClick = {
                        }
                    )
                    SettingsMenuLink(
                        title = { Text(text = "版本号") },
                        subtitle = {
                            Text(text = appVersionCode)
                        },
                        onClick = {
                        }
                    )
                }
                XhuSettingsGroup(title = {
                    Text(text = "西瓜课表团队出品")
                }) {
                    TeamItem(
                        painter = XhuIcons.Team.yue,
                        title = "图标以及界面设计",
                        subTitle = "@爱吃饭的越越",
                    )
                    TeamItem(
                        painter = XhuIcons.Team.pan,
                        title = "数据提供",
                        subTitle = "@pan",
                    )
                    TeamItem(
                        painter = XhuIcons.Team.johnny,
                        title = "产品设计",
                        subTitle = "@Johnny Zen",
                    )
                    TeamItem(
                        painter = XhuIcons.Team.quinn,
                        title = "微信小程序端开发",
                        subTitle = "@Quinn",
                    )
                    TeamItem(
                        painter = XhuIcons.Team.mystery0,
                        title = "Android端开发",
                        subTitle = "@Mystery0",
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

@Composable
private fun TeamItem(
    painter: Painter,
    title: String,
    subTitle: String,
    onClick: () -> Unit = {},
) {
    SettingsMenuLink(
        icon = {
            Icon(
                painter = painter,
                contentDescription = null,
                tint = Color.Unspecified,
            )
        },
        title = { Text(text = title) },
        subtitle = {
            Text(text = subTitle)
        },
        onClick = onClick,
    )
}