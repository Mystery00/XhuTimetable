package vip.mystery0.xhu.timetable.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BugReport
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.NotificationsNone
import androidx.compose.material.icons.rounded.Policy
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.material.icons.rounded.Translate
import androidx.compose.material.icons.rounded.VerifiedUser
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
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.datetime.LocalDate
import multiplatform.network.cmptoast.showToast
import org.koin.compose.viewmodel.koinViewModel
import vip.mystery0.xhu.timetable.Platform
import vip.mystery0.xhu.timetable.base.HandleErrorMessage
import vip.mystery0.xhu.timetable.base.appVersionCode
import vip.mystery0.xhu.timetable.base.appVersionName
import vip.mystery0.xhu.timetable.base.publicDeviceId
import vip.mystery0.xhu.timetable.config.coroutine.safeLaunch
import vip.mystery0.xhu.timetable.config.store.ConfigStore
import vip.mystery0.xhu.timetable.config.store.EventBus
import vip.mystery0.xhu.timetable.config.store.GlobalConfigStore
import vip.mystery0.xhu.timetable.config.store.PoemsStore
import vip.mystery0.xhu.timetable.config.store.setCacheStore
import vip.mystery0.xhu.timetable.config.store.setConfigStore
import vip.mystery0.xhu.timetable.model.event.EventType
import vip.mystery0.xhu.timetable.module.PRIVACY_URL
import vip.mystery0.xhu.timetable.platform
import vip.mystery0.xhu.timetable.ui.component.dialog.XhuSingleSelectDialog
import vip.mystery0.xhu.timetable.ui.component.dialog.rememberXhuDialogState
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
import vip.mystery0.xhu.timetable.ui.theme.XhuIcons
import vip.mystery0.xhu.timetable.ui.theme.showNightModeSelectList
import vip.mystery0.xhu.timetable.utils.MIN
import vip.mystery0.xhu.timetable.utils.copyToClipboard
import vip.mystery0.xhu.timetable.viewmodel.SettingsViewModel

@Composable
fun SettingsScreen() {
    val viewModel = koinViewModel<SettingsViewModel>()
    val navController = LocalNavController.current
    val uriHandler = LocalUriHandler.current

    val nightMode by viewModel.nightMode.collectAsState()
    val scope = rememberCoroutineScope()
    val nightModeDialog = rememberXhuDialogState()

    LaunchedEffect(Unit) {
        viewModel.init()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "软件设置",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            painter = XhuIcons.back,
                            contentDescription = "返回",
                        )
                    }
                },
            )
        },
    ) { paddingValues ->
        val layoutDirection = LocalLayoutDirection.current
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    top = paddingValues.calculateTopPadding(),
                    start = paddingValues.calculateStartPadding(layoutDirection),
                    end = paddingValues.calculateEndPadding(layoutDirection),
                )
                .verticalScroll(rememberScrollState())
                .padding(bottom = paddingValues.calculateBottomPadding()),
        ) {
            XhuSettingsGroup(title = { Text("外观与界面") }) {
                XhuSettingsMenuLink(
                    icon = {
                        Icon(
                            painter = XhuIcons.nightMode,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface,
                        )
                    },
                    title = { Text("深色与主题模式") },
                    subtitle = { Text("当前：${nightMode.title}") },
                    onClick = { nightModeDialog.show() }
                )
                XhuSettingsMenuLink(
                    icon = {
                        Icon(
                            painter = XhuIcons.customUi,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface,
                        )
                    },
                    title = { Text("自定义课表界面") },
                    subtitle = { Text("调整格子高度、圆角、字号、透明度与模板") },
                    onClick = { navController.navigateAndSave(RouteCustomUi) }
                )
                XhuSettingsMenuLink(
                    icon = {
                        Icon(
                            painter = XhuIcons.customBackground,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface,
                        )
                    },
                    title = { Text("自定义背景图片") },
                    subtitle = { Text("选择内置壁纸或从相册裁剪") },
                    onClick = { navController.navigateAndSave(RouteBackground) }
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
                    title = { Text("夜间模式自动禁用背景图") },
                    subtitle = { Text("开启深色模式时自动使用纯色暗底，保护视力") }
                ) {
                    EventBus.post(EventType.CHANGE_MAIN_BACKGROUND)
                }
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
                    title = { Text("启用月历日程视图") },
                    subtitle = {
                        val text = if (GlobalConfigStore.multiAccountMode) {
                            "多账号模式下暂不支持日历视图"
                        } else {
                            "以日程流形式浏览整学期课程与事项"
                        }
                        Text(text)
                    }
                ) {
                    EventBus.post(EventType.CHANGE_ENABLE_CALENDAR_VIEW)
                }
                XhuSettingsMenuLink(
                    icon = {
                        Icon(
                            painter = XhuIcons.clearSplash,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface,
                        )
                    },
                    title = { Text("重置启动图隐藏与背景缓存") },
                    subtitle = { Text("恢复启动图展示，或清除异常背景图") },
                    onClick = {
                        scope.safeLaunch {
                            setCacheStore { hideSplashBefore = LocalDate.MIN }
                            setConfigStore { backgroundImage = null }
                            EventBus.post(EventType.CHANGE_MAIN_BACKGROUND)
                        }
                        showToast("已重置启动图与背景设置")
                    }
                )
            }

            NotifySettings()

            XhuSettingsGroup(title = { Text("今日诗词") }) {
                PoemsSettingsCheckbox(
                    config = PoemsStore::disablePoems,
                    icon = {
                        Icon(
                            painter = XhuIcons.poems,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface,
                        )
                    },
                    title = { Text("禁用今日诗词") },
                    subtitle = { Text("关闭今日课程顶部的每日诗词卡片") },
                ) {
                    scope.safeLaunch { PoemsStore.disablePoems = it }
                    showToast("已保存，重启应用后生效")
                }
                PoemsSettingsCheckbox(
                    config = PoemsStore::showPoemsTranslate,
                    icon = {
                        Icon(
                            imageVector = Icons.Rounded.Translate,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface,
                        )
                    },
                    title = { Text("显示诗词大意") },
                    subtitle = { Text("展开诗词详情时附带译文解析") },
                ) {
                    scope.safeLaunch { PoemsStore.showPoemsTranslate = it }
                    showToast("已保存，重启应用后生效")
                }
                XhuSettingsMenuLink(
                    icon = {
                        Icon(
                            imageVector = Icons.Rounded.Refresh,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface,
                        )
                    },
                    title = { Text("重置诗词 Token") },
                    subtitle = { Text("若长期无法显示今日诗词，点击重置缓存凭证") },
                    onClick = {
                        scope.safeLaunch { viewModel.resetPoemsToken() }
                        showToast("重置成功，重启应用后生效")
                    }
                )
            }

            XhuSettingsGroup(title = { Text("关于与支持") }) {
                val pushRegistrationId by viewModel.pushRegistrationId.collectAsState()
                XhuSettingsMenuLink(
                    icon = {
                        Icon(
                            imageVector = Icons.Rounded.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface,
                        )
                    },
                    title = { Text("版本与更新日志") },
                    subtitle = { Text("当前版本：${appVersionName()} (${appVersionCode()})") },
                    onClick = {
                        if (viewModel.clickVersion(3000L)) {
                            viewModel.enableDebugMode()
                            showToast("开发者模式已启用！")
                        } else {
                            uriHandler.openUri("https://blog.mystery0.vip/docs/xgkb/changelog")
                        }
                    }
                )
                UpdateSettings()
                XhuSettingsMenuLink(
                    icon = {
                        Icon(
                            imageVector = Icons.Rounded.Language,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface,
                        )
                    },
                    title = { Text("西瓜课表官网") },
                    subtitle = { Text("https://xgkb.mystery0.vip") },
                    onClick = { uriHandler.openUri("https://xgkb.mystery0.vip") }
                )
                XhuSettingsMenuLink(
                    icon = {
                        Icon(
                            painter = XhuIcons.github,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface,
                        )
                    },
                    title = { Text("开源仓库") },
                    subtitle = { Text("https://github.com/Mystery00/XhuTimetable") },
                    onClick = { uriHandler.openUri("https://github.com/Mystery00/XhuTimetable") }
                )
                XhuSettingsMenuLink(
                    icon = {
                        Icon(
                            painter = XhuIcons.poems,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface,
                        )
                    },
                    title = { Text("致谢 · 今日诗词") },
                    subtitle = { Text("感谢作者 @xenv，点击访问官网") },
                    onClick = { uriHandler.openUri("https://www.jinrishici.com") }
                )
                XhuSettingsMenuLink(
                    icon = {
                        Icon(
                            imageVector = Icons.Rounded.Policy,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface,
                        )
                    },
                    title = { Text("隐私政策") },
                    onClick = { uriHandler.openUri(PRIVACY_URL) }
                )
                XhuSettingsMenuLink(
                    icon = {
                        Icon(
                            imageVector = Icons.Rounded.VerifiedUser,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface,
                        )
                    },
                    title = { Text("ICP 备案号") },
                    subtitle = { Text("蜀ICP备19031621号-2A") },
                    onClick = { uriHandler.openUri("https://beian.miit.gov.cn") }
                )
                XhuSettingsMenuLink(
                    icon = {
                        Icon(
                            imageVector = Icons.Rounded.Description,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface,
                        )
                    },
                    title = { Text("开源依赖项说明") },
                    onClick = { navController.navigateAndSave(RouteAbout) }
                )
                XhuSettingsMenuLink(
                    icon = {
                        Icon(
                            imageVector = Icons.Rounded.NotificationsNone,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface,
                        )
                    },
                    title = { Text("推送注册 ID") },
                    subtitle = { Text(pushRegistrationId.ifBlank { "暂无" }) },
                    onClick = {
                        if (pushRegistrationId.isBlank()) {
                            showToast("暂无推送注册id")
                        } else {
                            copyToClipboard(pushRegistrationId)
                            showToast("已复制推送注册 ID")
                        }
                    },
                )
                XhuSettingsMenuLink(
                    icon = {
                        Icon(
                            imageVector = Icons.Rounded.BugReport,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface,
                        )
                    },
                    title = { Text("上报诊断日志") },
                    subtitle = { Text("向开发者分享运行日志以辅助排查问题") },
                    onClick = { viewModel.reportLog() }
                )
            }

            val teamMember by viewModel.teamMemberData.collectAsState()
            if (teamMember.isNotEmpty()) {
                TeamContent(title = "西瓜课表团队出品") {
                    teamMember.forEach {
                        TeamItem(
                            iconUrl = it.icon,
                            title = it.title,
                            subTitle = it.subtitle,
                        )
                    }
                }
            }

            val debugMode by viewModel.debugMode.collectAsState()
            XhuFoldSettingsGroup(
                title = { Text("开发者选项") },
                foldState = !debugMode,
            ) {
                XhuActionSettingsCheckbox(
                    title = { Text("启用开发者模式") },
                    onCheckedChange = { viewModel.disableDebugMode() },
                    checked = true,
                    onClick = {}
                )
                ConfigSettingsCheckbox(
                    config = ConfigStore::alwaysCrash,
                    enabled = platform() == Platform.ANDROID,
                    scope = scope,
                    title = { Text("始终显示崩溃信息") },
                    subtitle = { Text("不捕获全局异常") }
                )
                val splashList by viewModel.splashList.collectAsState()
                XhuSettingsMenuLink(
                    title = { Text("启动页信息") },
                    subtitle = { Text(splashList.toString()) },
                    onClick = {},
                )
                XhuSettingsMenuLink(
                    title = { Text("设备 ID") },
                    subtitle = { Text(publicDeviceId()) },
                    onClick = {
                        copyToClipboard(publicDeviceId())
                        showToast("已复制设备 ID")
                    },
                )
                val featurePullTimeList by viewModel.featurePullTimeList.collectAsState()
                XhuSettingsMenuLink(
                    title = { Text("FeatureHub 最近拉取时间") },
                    subtitle = { Text(featurePullTimeList.joinToString("\n")) },
                    onClick = { viewModel.updateFeaturePullTime() }
                )
                XhuSettingsMenuLink(
                    title = { Text("推送测试") },
                    subtitle = { Text("请求服务端发送测试推送") },
                    onClick = { viewModel.startPushTest() },
                )
                DeveloperSettings()
                XhuSettingsMenuLink(
                    icon = {
                        Icon(
                            imageVector = Icons.Rounded.RestartAlt,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface,
                        )
                    },
                    title = { Text("撤销隐私授权") },
                    onClick = {
                        scope.safeLaunch { setCacheStore { allowPrivacy = false } }
                        showToast("已撤销隐私授权")
                    },
                )
                XhuSettingsMenuLink(
                    title = { Text("测试崩溃") },
                    onClick = {
                        scope.safeLaunch { throw RuntimeException("this is a test exception") }
                    },
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    val nightModeList = showNightModeSelectList()
    XhuSingleSelectDialog(
        state = nightModeDialog,
        title = "深色与主题模式",
        options = nightModeList,
        selectedIndex = nightModeList.indexOf(nightMode).coerceAtLeast(0),
        itemLabel = { it.title },
        onSelect = { _, select -> viewModel.updateNightMode(select) },
    )

    HandleErrorMessage(flow = viewModel.errorMessage)
}

@Composable
expect fun NotifySettings()

@Composable
expect fun UpdateSettings()

@Composable
expect fun DeveloperSettings()
