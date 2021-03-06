package vip.mystery0.xhu.timetable.ui.activity

import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.alorma.compose.settings.ui.SettingsMenuLink
import com.microsoft.appcenter.crashes.model.TestCrashException
import com.vanpra.composematerialdialogs.*
import com.vanpra.composematerialdialogs.datetime.time.timepicker
import kotlinx.coroutines.launch
import vip.mystery0.xhu.timetable.*
import vip.mystery0.xhu.timetable.base.BaseComposeActivity
import vip.mystery0.xhu.timetable.config.DataHolder
import vip.mystery0.xhu.timetable.config.GlobalConfig
import vip.mystery0.xhu.timetable.config.chinaZone
import vip.mystery0.xhu.timetable.config.setConfig
import vip.mystery0.xhu.timetable.model.entity.NightMode
import vip.mystery0.xhu.timetable.model.entity.nightModeSelectList
import vip.mystery0.xhu.timetable.model.event.EventType
import vip.mystery0.xhu.timetable.model.event.UIEvent
import vip.mystery0.xhu.timetable.ui.preference.ConfigSettingsCheckbox
import vip.mystery0.xhu.timetable.ui.preference.XhuFoldSettingsGroup
import vip.mystery0.xhu.timetable.ui.preference.XhuSettingsGroup
import vip.mystery0.xhu.timetable.ui.theme.XhuColor
import vip.mystery0.xhu.timetable.ui.theme.XhuIcons
import vip.mystery0.xhu.timetable.utils.chinaDateTimeFormatter
import vip.mystery0.xhu.timetable.utils.timeFormatter
import vip.mystery0.xhu.timetable.viewmodel.SettingsViewModel
import java.time.Instant
import java.time.LocalDateTime
import java.time.LocalTime

class SettingsActivity : BaseComposeActivity() {
    private val viewModel: SettingsViewModel by viewModels()

    @Composable
    override fun BuildContent() {
        val notifyTime by viewModel.notifyTimeData.collectAsState()
        val scope = rememberCoroutineScope()

        val nightMode by viewModel.nightMode.collectAsState()

        val showNightModeState = rememberMaterialDialogState()
        val showNotifyTimeState = rememberMaterialDialogState()

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
                    SettingsMenuLink(
                        icon = {
                            Icon(
                                painter = XhuIcons.customBackground,
                                contentDescription = null,
                                tint = XhuColor.Common.blackText,
                            )
                        },
                        title = { Text(text = "?????????????????????") },
                        onClick = {
                            intentTo(BackgroundActivity::class)
                        }
                    )
                    ConfigSettingsCheckbox(
                        config = GlobalConfig::disableBackgroundWhenNight,
                        scope = scope,
                        icon = {
                            Icon(
                                painter = XhuIcons.disableBackgroundWhenNight,
                                contentDescription = null,
                                tint = XhuColor.Common.blackText,
                            )
                        },
                        title = { Text(text = "????????????????????????????????????") },
                        subtitle = {
                            Text(text = "???????????????????????????????????????????????????")
                        },
                        onCheckedChange = {
                            setConfig { disableBackgroundWhenNight = it }
                            eventBus.post(UIEvent(EventType.CHANGE_MAIN_BACKGROUND))
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
                        title = { Text(text = "????????????") },
                        onClick = {
                            showNightModeState.show()
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
                        title = { Text(text = "??????????????????") },
                        onCheckedChange = {
                            setConfig { enablePageEffect = it }
                            eventBus.post(UIEvent(EventType.CHANGE_PAGE_EFFECT))
                        }
                    )
                    SettingsMenuLink(
                        icon = {
                            Icon(
                                painter = XhuIcons.clearSplash,
                                contentDescription = null,
                                tint = XhuColor.Common.blackText,
                            )
                        },
                        title = { Text(text = "???????????????????????????") },
                        onClick = {
                            scope.launch {
                                setConfig { hideSplashBefore = Instant.ofEpochMilli(0L) }
                                "????????????".toast()
                            }
                        }
                    )
                    SettingsMenuLink(
                        icon = {
                            Icon(
                                painter = XhuIcons.customUi,
                                contentDescription = null,
                                tint = XhuColor.Common.blackText,
                            )
                        },
                        title = { Text(text = "?????????????????????") },
                        onClick = {
                            intentTo(CustomUiActivity::class)
                        }
                    )
                }
                XhuSettingsGroup(title = {
                    Text(text = "????????????")
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
                        title = { Text(text = "????????????") },
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
                        title = { Text(text = "????????????") },
                        onCheckedChange = {
                            setConfig { notifyExam = it }
                        }
                    )
                    SettingsMenuLink(
                        title = { Text(text = "????????????") },
                        icon = {
                            Icon(
                                painter = XhuIcons.notifyTime,
                                contentDescription = null,
                                tint = XhuColor.Common.blackText,
                            )
                        },
                        subtitle = {
                            Text(
                                text = if (notifyTime != null)
                                    "?????????????????? ${notifyTime!!.format(timeFormatter)} ??????\n?????????????????????????????????${appName}?????????????????????????????????"
                                else
                                    "?????????????????????"
                            )
                        },
                        action = {
                            Checkbox(
                                checked = notifyTime != null,
                                onCheckedChange = {
                                    viewModel.updateNotifyTime(null)
                                },
                                enabled = notifyTime != null,
                            )
                        },
                        onClick = {
                            showNotifyTimeState.show()
                        }
                    )
                }
                XhuSettingsGroup(title = {
                    Text(text = "????????????")
                }) {
                    ConfigSettingsCheckbox(
                        config = GlobalConfig::disablePoems,
                        scope = scope,
                        title = { Text(text = "??????????????????") },
                        onCheckedChange = {
                            setConfig { disablePoems = it }
                            "?????????????????????".toast()
                        }
                    )
                    ConfigSettingsCheckbox(
                        config = GlobalConfig::showPoemsTranslate,
                        scope = scope,
                        title = { Text(text = "??????????????????") },
                        onCheckedChange = {
                            setConfig { showPoemsTranslate = it }
                            "?????????????????????".toast()
                        }
                    )
                }
                XhuSettingsGroup(title = {
                    Text(text = "????????????")
                }) {
                    SettingsMenuLink(
                        icon = {
                            Icon(
                                painter = XhuIcons.checkUpdate,
                                contentDescription = null,
                                tint = XhuColor.Common.blackText,
                            )
                        },
                        title = { Text(text = "????????????") },
                        onClick = {
                            scope.launch {
                                "????????????".toast()
                            }
                        }
                    )
                    ConfigSettingsCheckbox(
                        config = GlobalConfig::showOldCourseWhenFailed,
                        scope = scope,
                        title = { Text(text = "???????????????????????????????????????????????????????????????") },
                        subtitle = { Text(text = "????????????????????????????????????????????????") },
                        onCheckedChange = {
                            setConfig { showOldCourseWhenFailed = it }
                        }
                    )
                    ConfigSettingsCheckbox(
                        icon = {
                            Icon(
                                painter = XhuIcons.allowUploadCrash,
                                contentDescription = null,
                                tint = XhuColor.Common.blackText,
                            )
                        },
                        config = GlobalConfig::allowSendCrashReport,
                        scope = scope,
                        title = { Text(text = "??????????????????") },
                        subtitle = {
                            Text(text = "????????????????????????????????????????????????")
                        },
                        onCheckedChange = {
                            setConfig { allowSendCrashReport = it }
                        }
                    )
                    SettingsMenuLink(
                        title = { },
                        subtitle = {
                            Text(
                                text = """
                              ????????????Visual Studio App Center ??????Microsoft????????????
                              
                              ???????????????????????????SSAID?????? Android 8.0???API ?????? 26????????????????????????SSAID ??????????????????????????????????????????????????????????????????????????????????????????
                          """.trimIndent()
                            )
                        },
                        onClick = {},
                    )
                    SettingsMenuLink(
                        icon = {
                            Icon(
                                painter = XhuIcons.qqGroup,
                                contentDescription = null,
                                tint = XhuColor.Common.blackText,
                            )
                        },
                        title = { Text(text = "????????????????????????????????????????????????") },
                        onClick = {
                            joinQQGroup(this@SettingsActivity)
                        }
                    )
                }
                XhuSettingsGroup(title = {
                    Text(text = "????????????")
                }) {
                    SettingsMenuLink(
                        title = { Text(text = "??????????????????") },
                        onClick = {
                            scope.launch {
                                setConfig { backgroundImage = null }
                                "??????????????????".toast()
                            }
                        }
                    )
                }
                XhuSettingsGroup(title = {
                    Text(text = "????????????")
                }) {
                    SettingsMenuLink(
                        title = { Text(text = "????????????") },
                        onClick = {
                            scope.launch {
                                "????????????".toast()
                            }
                        }
                    )
                    SettingsMenuLink(
                        icon = {
                            Icon(
                                painter = XhuIcons.github,
                                contentDescription = null,
                                tint = XhuColor.Common.blackText,
                            )
                        },
                        title = { Text(text = "????????????") },
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
                        title = { Text(text = "????????????") },
                        subtitle = {
                            Text(text = "????????????@xenv???????????????????????????????????????")
                        },
                        onClick = {
                            loadInBrowser("https://www.jinrishici.com")
                        }
                    )
                }
                XhuSettingsGroup(title = {
                    Text(text = "????????????")
                }) {
                    SettingsMenuLink(
                        title = { Text(text = "????????????") },
                        subtitle = {
                            Text(text = appVersionName)
                        },
                        onClick = {
                        }
                    )
                    SettingsMenuLink(
                        title = { Text(text = "?????????") },
                        subtitle = {
                            Text(text = appVersionCode)
                        },
                        onClick = {
                            if (viewModel.clickVersion(3000L)) {
                                viewModel.enableDebugMode()
                                toastString("???????????????????????????")
                            }
                        }
                    )
                }
                XhuSettingsGroup(title = {
                    Text(text = "????????????????????????")
                }) {
                    val teamMember by viewModel.teamMemberData.collectAsState()
                    teamMember.forEach {
                        TeamItem(
                            painter = rememberAsyncImagePainter(
                                ImageRequest.Builder(LocalContext.current)
                                    .data(data = it.icon)
                                    .apply {
                                        size(width = 192, height = 192)
                                    }
                                    .build()
                            ),
                            title = it.title,
                            subTitle = it.subtitle,
                        )
                    }
                }
                val debugMode by viewModel.debugMode.collectAsState()
                if (debugMode) {
                    XhuFoldSettingsGroup(
                        title = {
                            Text(text = "???????????????")
                        },
                    ) {
                        SettingsMenuLink(
                            title = { Text(text = "?????????????????????") },
                            action = {
                                Checkbox(
                                    checked = debugMode,
                                    onCheckedChange = {
                                        viewModel.disableDebugMode()
                                    },
                                    enabled = debugMode,
                                )
                            },
                            onClick = {},
                        )
                        SettingsMenuLink(
                            title = { Text(text = "????????????") },
                            onClick = {
                                scope.launch {
                                    throw TestCrashException()
                                }
                            },
                        )
                        val splashList by viewModel.splashList.collectAsState()
                        SettingsMenuLink(
                            title = { Text(text = "???????????????") },
                            subtitle = {
                                Text(text = splashList.toString())
                            },
                            onClick = {
                            },
                        )
                        val version = DataHolder.version
                        SettingsMenuLink(
                            title = { Text(text = "???????????????") },
                            subtitle = {
                                Text(text = version?.toString() ?: "?????????")
                            },
                            onClick = {
                            },
                        )
                        ConfigSettingsCheckbox(
                            config = GlobalConfig::alwaysShowNewVersion,
                            scope = scope,
                            title = { Text(text = "???????????????????????????") },
                            onCheckedChange = {
                                setConfig { alwaysShowNewVersion = it }
                            }
                        )
                        SettingsMenuLink(
                            title = { Text(text = "???????????????????????????") },
                            subtitle = {
                                Text(text = version?.apkSize?.formatFileSize() ?: "?????????")
                            },
                            onClick = {
                                if (version != null) {
                                    viewModel.downloadApk()
                                }
                            },
                        )
                        SettingsMenuLink(
                            title = { Text(text = "???????????????????????????") },
                            subtitle = {
                                Text(text = version?.patchSize?.formatFileSize() ?: "?????????")
                            },
                            onClick = {
                                if (version != null) {
                                    viewModel.downloadPatch()
                                }
                            },
                        )
                        SettingsMenuLink(
                            title = { Text(text = "NotifyWork ?????????????????????") },
                            subtitle = {
                                Text(
                                    text = LocalDateTime.ofInstant(
                                        GlobalConfig.notifyWorkLastExecuteTime,
                                        chinaZone
                                    ).format(chinaDateTimeFormatter)
                                )
                            },
                            onClick = {
                            }
                        )
                        SettingsMenuLink(
                            title = { Text(text = "PullWork ?????????????????????") },
                            subtitle = {
                                Text(
                                    text = LocalDateTime.ofInstant(
                                        GlobalConfig.pullWorkLastExecuteTime,
                                        chinaZone
                                    ).format(chinaDateTimeFormatter)
                                )
                            },
                            onClick = {
                            }
                        )
                    }
                }
            }
        }
        val errorMessage by viewModel.errorMessage.collectAsState()
        if (errorMessage.isNotBlank()) {
            errorMessage.toast(true)
        }
        BuildNightModeSelector(
            dialogState = showNightModeState,
            initNightMode = nightMode,
        )
        BuildTimeSelector(
            dialogState = showNotifyTimeState,
            initTime = notifyTime ?: LocalTime.now(),
        )
    }

    @Composable
    private fun BuildNightModeSelector(
        dialogState: MaterialDialogState,
        initNightMode: NightMode,
    ) {
        val list = nightModeSelectList()
        var selectedMode = list.indexOf(initNightMode)
        if (selectedMode == -1) selectedMode = 0
        MaterialDialog(
            dialogState = dialogState,
            buttons = {
                positiveButton("??????") {
                    viewModel.updateNightMode(list[selectedMode])
                }
                negativeButton("??????")
            }) {
            title("????????????")
            listItemsSingleChoice(
                list = list.map { it.title },
                initialSelection = selectedMode,
            ) {
                selectedMode = it
            }
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
                    viewModel.updateNotifyTime(selectedTime)
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
                modifier = Modifier.size(48.dp),
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