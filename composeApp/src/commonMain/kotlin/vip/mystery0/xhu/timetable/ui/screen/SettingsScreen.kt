package vip.mystery0.xhu.timetable.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.platform.LocalUriHandler
import com.maxkeppeker.sheets.core.models.base.UseCaseState
import com.maxkeppeker.sheets.core.models.base.rememberUseCaseState
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import multiplatform.network.cmptoast.showToast
import org.koin.compose.viewmodel.koinViewModel
import vip.mystery0.xhu.timetable.base.HandleErrorMessage
import vip.mystery0.xhu.timetable.base.appVersionCode
import vip.mystery0.xhu.timetable.base.publicDeviceId
import vip.mystery0.xhu.timetable.config.store.ConfigStore
import vip.mystery0.xhu.timetable.config.store.EventBus
import vip.mystery0.xhu.timetable.config.store.GlobalConfigStore
import vip.mystery0.xhu.timetable.config.store.PoemsStore
import vip.mystery0.xhu.timetable.config.store.setCacheStore
import vip.mystery0.xhu.timetable.config.store.setConfigStore
import vip.mystery0.xhu.timetable.model.event.EventType
import vip.mystery0.xhu.timetable.ui.component.ShowSingleSelectDialog
import vip.mystery0.xhu.timetable.ui.component.preference.ConfigSettingsCheckbox
import vip.mystery0.xhu.timetable.ui.component.preference.PoemsSettingsCheckbox
import vip.mystery0.xhu.timetable.ui.component.preference.TeamContent
import vip.mystery0.xhu.timetable.ui.component.preference.TeamItem
import vip.mystery0.xhu.timetable.ui.component.preference.XhuActionSettingsCheckbox
import vip.mystery0.xhu.timetable.ui.component.preference.XhuFoldSettingsGroup
import vip.mystery0.xhu.timetable.ui.component.preference.XhuSettingsGroup
import vip.mystery0.xhu.timetable.ui.component.preference.XhuSettingsMenuLink
import vip.mystery0.xhu.timetable.ui.navigation.LocalNavController
import vip.mystery0.xhu.timetable.ui.navigation.RouteAbout
import vip.mystery0.xhu.timetable.ui.navigation.RouteBackground
import vip.mystery0.xhu.timetable.ui.navigation.RouteCustomUi
import vip.mystery0.xhu.timetable.ui.navigation.navigateAndSave
import vip.mystery0.xhu.timetable.ui.theme.NightMode
import vip.mystery0.xhu.timetable.ui.theme.XhuIcons
import vip.mystery0.xhu.timetable.ui.theme.showNightModeSelectList
import vip.mystery0.xhu.timetable.utils.MIN
import vip.mystery0.xhu.timetable.utils.copyToClipboard
import vip.mystery0.xhu.timetable.viewmodel.SettingsViewModel

@Composable
fun SettingsScreen() {
    val viewModel = koinViewModel<SettingsViewModel>()

    val navController = LocalNavController.current!!
    val uriHandler = LocalUriHandler.current

    val nightMode by viewModel.nightMode.collectAsState()

    val scope = rememberCoroutineScope()
    val showNightModeState = rememberUseCaseState()

    LaunchedEffect(Unit) {
        viewModel.init()
    }
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = "软件设置") },
                navigationIcon = {
                    IconButton(onClick = {
                        navController.popBackStack()
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
                Text(text = "界面设置")
            }) {
                XhuSettingsMenuLink(
                    icon = {
                        Icon(
                            painter = XhuIcons.customBackground,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface,
                        )
                    },
                    title = { Text(text = "自定义背景图片") },
                    onClick = {
                        navController.navigateAndSave(RouteBackground)
                    }
                )
                XhuSettingsMenuLink(
                    title = { Text(text = "强制重置背景图") },
                    onClick = {
                        scope.launch {
                            setConfigStore { backgroundImage = null }
                            EventBus.post(EventType.CHANGE_MAIN_BACKGROUND)
                            showToast("重置成功")
                        }
                    }
                )
                ConfigSettingsCheckbox(
                    config = ConfigStore::disableBackgroundWhenNight,
                    scope = scope,
                    icon = {
                        Icon(
                            painter = XhuIcons.disableBackgroundWhenNight,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface,
                        )
                    },
                    title = { Text(text = "夜间模式时自动禁用背景图") },
                    subtitle = {
                        Text(text = "当夜间模式开启时，自动禁用背景图片")
                    }
                ) {
                    EventBus.post(EventType.CHANGE_MAIN_BACKGROUND)
                }
                XhuSettingsMenuLink(
                    icon = {
                        Icon(
                            painter = XhuIcons.nightMode,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface,
                        )
                    },
                    title = { Text(text = "夜间模式") },
                    onClick = {
                        showNightModeState.show()
                    }
                )
                XhuSettingsMenuLink(
                    icon = {
                        Icon(
                            painter = XhuIcons.clearSplash,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface,
                        )
                    },
                    title = { Text(text = "清除启动图隐藏设置") },
                    onClick = {
                        scope.launch {
                            setCacheStore { hideSplashBefore = LocalDate.MIN }
                            showToast("清理成功")
                        }
                    }
                )
                XhuSettingsMenuLink(
                    icon = {
                        Icon(
                            painter = XhuIcons.customUi,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface,
                        )
                    },
                    title = { Text(text = "自定义课表界面") },
                    onClick = {
                        navController.navigateAndSave(RouteCustomUi)
                    }
                )
                ConfigSettingsCheckbox(
                    config = ConfigStore::enableCalendarView,
                    enabled = !GlobalConfigStore.multiAccountMode,
                    scope = scope,
                    icon = {
                        Icon(
                            painter = XhuIcons.enableCalendarView,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface,
                        )
                    },
                    title = { Text(text = "启用日历视图") },
                    subtitle = {
                        val text = buildString {
                            append("启用后，可以以日历的视图中查看当前学期的所有课程")
                            if (GlobalConfigStore.multiAccountMode) {
                                appendLine()
                                append("【多账号模式下无法使用日历视图】")
                            }
                        }
                        Text(text = text)
                    }
                ) {
                    EventBus.post(EventType.CHANGE_ENABLE_CALENDAR_VIEW)
                }
            }
            NotifySettings()
            XhuSettingsGroup(title = {
                Text(text = "诗词设置")
            }) {
                PoemsSettingsCheckbox(
                    config = PoemsStore::disablePoems,
                    title = { Text(text = "禁用今日诗词") }
                ) {
                    PoemsStore.disablePoems = it
                    showToast("重启应用后生效")
                }
                PoemsSettingsCheckbox(
                    config = PoemsStore::showPoemsTranslate,
                    title = { Text(text = "显示诗词大意") }
                ) {
                    PoemsStore.showPoemsTranslate = it
                    showToast("重启应用后生效")
                }
                XhuSettingsMenuLink(
                    title = { Text(text = "重置Token") },
                    subtitle = { Text(text = "如果一直无法显示今日诗词，可能是缓存的Token出现了问题，点击此处可以进行重置") },
                    onClick = {
                        scope.launch {
                            viewModel.resetPoemsToken()
                            showToast("重置成功，重启应用后生效")
                        }
                    }
                )
            }
            XhuSettingsGroup(title = {
                Text(text = "应用关于")
            }) {
                XhuSettingsMenuLink(
                    title = { Text(text = "更新日志") },
                    onClick = {
                        uriHandler.openUri("https://blog.mystery0.vip/xgkb-changelog")
                    }
                )
                XhuSettingsMenuLink(
                    title = { Text(text = "西瓜课表官网") },
                    subtitle = {
                        Text(text = "点击访问 https://xgkb.mystery0.vip")
                    },
                    onClick = {
                        uriHandler.openUri("https://xgkb.mystery0.vip")
                    }
                )
                XhuSettingsMenuLink(
                    icon = {
                        Icon(
                            painter = XhuIcons.github,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface,
                        )
                    },
                    title = { Text(text = "开源地址") },
                    subtitle = {
                        Text(text = "https://github.com/Mystery00/XhuTimetable")
                    },
                    onClick = {
                        uriHandler.openUri("https://github.com/Mystery00/XhuTimetable")
                    }
                )
                XhuSettingsMenuLink(
                    icon = {
                        Icon(
                            painter = XhuIcons.poems,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface,
                        )
                    },
                    title = { Text(text = "今日诗词") },
                    subtitle = {
                        Text(text = "感谢作者@xenv，点击访问『今日诗词』官网")
                    },
                    onClick = {
                        uriHandler.openUri("https://www.jinrishici.com")
                    }
                )
            }
            XhuSettingsGroup(title = {
                Text(text = "版本关于")
            }) {
                XhuSettingsMenuLink(
                    title = { Text(text = "ICP备案号") },
                    subtitle = {
                        Text(text = "蜀ICP备19031621号-2A")
                    },
                    onClick = {
                        uriHandler.openUri("https://beian.miit.gov.cn")
                    }
                )
                XhuSettingsMenuLink(
                    title = { Text(text = "版本号") },
                    subtitle = {
                        Text(text = appVersionCode())
                    },
                    onClick = {
                        if (viewModel.clickVersion(3000L)) {
                            viewModel.enableDebugMode()
                            showToast("开发者模式已启用！")
                        }
                    }
                )
                UpdateSettings()
                XhuSettingsMenuLink(
                    title = { Text(text = "依赖项说明") },
                    onClick = {
                        navController.navigateAndSave(RouteAbout)
                    }
                )
                XhuSettingsMenuLink(
                    title = { Text(text = "日志上报") },
                    onClick = {
                        showToast("待实现")
                    }
                )
            }
            TeamContent(title = "西瓜课表团队出品") {
                val teamMember by viewModel.teamMemberData.collectAsState()
                teamMember.forEach {
                    TeamItem(
                        iconUrl = it.icon,
                        title = it.title,
                        subTitle = it.subtitle,
                    )
                }
            }
            val debugMode by viewModel.debugMode.collectAsState()
            if (debugMode) {
                XhuFoldSettingsGroup(
                    title = {
                        Text(text = "开发者选项")
                    },
                ) {
                    XhuActionSettingsCheckbox(
                        title = { Text(text = "启用开发者模式") },
                        onCheckedChange = {
                            viewModel.disableDebugMode()
                        },
                        checked = true,
                        onClick = { }
                    )
                    ConfigSettingsCheckbox(
                        config = ConfigStore::alwaysCrash,
                        scope = scope,
                        title = { Text(text = "始终显示崩溃信息") },
                        subtitle = {
                            Text(text = "不捕获全局异常")
                        }
                    )
                    val splashList by viewModel.splashList.collectAsState()
                    XhuSettingsMenuLink(
                        title = { Text(text = "启动页信息") },
                        subtitle = {
                            Text(text = splashList.toString())
                        },
                        onClick = {
                        },
                    )
                    XhuSettingsMenuLink(
                        title = { Text(text = "设备id") },
                        subtitle = {
                            Text(text = publicDeviceId())
                        },
                        onClick = {
                            copyToClipboard(publicDeviceId())
                        },
                    )
                    val featurePullTimeList by viewModel.featurePullTimeList.collectAsState()
                    XhuSettingsMenuLink(
                        title = { Text(text = "FeatureHub 最近拉取时间") },
                        subtitle = {
                            Text(
                                text = featurePullTimeList.joinToString("\n")
                            )
                        },
                        onClick = {
                            viewModel.updateFeaturePullTime()
                        }
                    )
                    DeveloperSettings()
                }
            }
        }
    }
    BuildNightModeSelector(
        useCaseState = showNightModeState,
        initNightMode = nightMode,
        onSelect = {
            viewModel.updateNightMode(it)
        }
    )

    HandleErrorMessage(flow = viewModel.errorMessage)
}

@Composable
private fun BuildNightModeSelector(
    useCaseState: UseCaseState,
    initNightMode: NightMode,
    onSelect: (NightMode) -> Unit
) {
    val list = showNightModeSelectList()
    var selectedMode = list.indexOf(initNightMode)
    if (selectedMode == -1) selectedMode = 0

    ShowSingleSelectDialog(
        dialogTitle = "更改主题",
        options = list,
        selectIndex = selectedMode,
        itemTransform = { it.title },
        useCaseState = useCaseState,
        onSelect = { _, select ->
            onSelect(select)
        },
    )
}

@Composable
expect fun NotifySettings()

@Composable
expect fun UpdateSettings()

@Composable
expect fun DeveloperSettings()