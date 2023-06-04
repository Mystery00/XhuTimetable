package vip.mystery0.xhu.timetable.ui.activity

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.net.Uri
import android.provider.Settings
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.microsoft.appcenter.crashes.model.TestCrashException
import com.vanpra.composematerialdialogs.*
import com.vanpra.composematerialdialogs.datetime.time.timepicker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.core.component.inject
import vip.mystery0.xhu.timetable.*
import vip.mystery0.xhu.timetable.R
import vip.mystery0.xhu.timetable.base.BaseComposeActivity
import vip.mystery0.xhu.timetable.config.store.CacheStore
import vip.mystery0.xhu.timetable.config.store.ConfigStore
import vip.mystery0.xhu.timetable.config.store.EventBus
import vip.mystery0.xhu.timetable.config.store.GlobalCacheStore
import vip.mystery0.xhu.timetable.config.store.PoemsStore
import vip.mystery0.xhu.timetable.config.store.setCacheStore
import vip.mystery0.xhu.timetable.config.store.setConfigStore
import vip.mystery0.xhu.timetable.model.entity.NightMode
import vip.mystery0.xhu.timetable.model.entity.VersionChannel
import vip.mystery0.xhu.timetable.model.event.EventType
import vip.mystery0.xhu.timetable.model.response.ClientVersion
import vip.mystery0.xhu.timetable.repository.StartRepo
import vip.mystery0.xhu.timetable.ui.activity.contract.FontFileResultContract
import vip.mystery0.xhu.timetable.ui.preference.CacheSettingsCheckbox
import vip.mystery0.xhu.timetable.ui.preference.ConfigSettingsCheckbox
import vip.mystery0.xhu.timetable.ui.preference.PoemsSettingsCheckbox
import vip.mystery0.xhu.timetable.ui.preference.XhuFoldSettingsGroup
import vip.mystery0.xhu.timetable.ui.preference.XhuSettingsGroup
import vip.mystery0.xhu.timetable.ui.preference.XhuSettingsMenuLink
import vip.mystery0.xhu.timetable.ui.theme.XhuColor
import vip.mystery0.xhu.timetable.ui.theme.XhuIcons
import vip.mystery0.xhu.timetable.utils.formatChinaDateTime
import vip.mystery0.xhu.timetable.utils.timeFormatter
import vip.mystery0.xhu.timetable.viewmodel.SettingsViewModel
import java.time.LocalDate
import java.time.LocalTime

class SettingsActivity : BaseComposeActivity() {
    private val viewModel: SettingsViewModel by viewModels()
    private val clipboardManager: ClipboardManager by inject()

    private val fontFileSelectLauncher =
        registerForActivityResult(FontFileResultContract()) { intent ->
            if (intent == null) {
                toastString("操作已取消")
                return@registerForActivityResult
            }
            intent.data?.let {
                viewModel.setCustomFont(it)
                toastString("字体设置成功，重启应用后生效")
            }
        }

    @SuppressLint("BatteryLife")
    @Composable
    override fun BuildContent() {
        val notifyTime by viewModel.notifyTimeData.collectAsState()
        val scope = rememberCoroutineScope()

        val nightMode by viewModel.nightMode.collectAsState()
        val versionChannel by viewModel.versionChannel.collectAsState()

        val showNightModeState = rememberMaterialDialogState()
        val showNotifyTimeState = rememberMaterialDialogState()
        val showUpdateLogState = rememberMaterialDialogState()
        val checkVersionChannelState = rememberMaterialDialogState()

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
                    XhuSettingsMenuLink(
                        icon = {
                            Icon(
                                painter = XhuIcons.customBackground,
                                contentDescription = null,
                                tint = XhuColor.Common.blackText,
                            )
                        },
                        title = { Text(text = "自定义背景图片") },
                        onClick = {
                            intentTo(BackgroundActivity::class)
                        }
                    )
                    ConfigSettingsCheckbox(
                        config = ConfigStore::disableBackgroundWhenNight,
                        scope = scope,
                        icon = {
                            Icon(
                                painter = XhuIcons.disableBackgroundWhenNight,
                                contentDescription = null,
                                tint = XhuColor.Common.blackText,
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
                                tint = XhuColor.Common.blackText,
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
                                tint = XhuColor.Common.blackText,
                            )
                        },
                        title = { Text(text = "清除启动图隐藏设置") },
                        onClick = {
                            scope.launch {
                                setCacheStore { hideSplashBefore = LocalDate.MIN }
                                "清理成功".toast()
                            }
                        }
                    )
                    XhuSettingsMenuLink(
                        icon = {
                            Icon(
                                painter = XhuIcons.customUi,
                                contentDescription = null,
                                tint = XhuColor.Common.blackText,
                            )
                        },
                        title = { Text(text = "自定义课表界面") },
                        onClick = {
                            intentTo(CustomUiActivity::class)
                        }
                    )
                    XhuSettingsMenuLink(
                        title = { Text(text = "自定义字体") },
                        subtitle = {
                            Text(text = "注意，如果选择的字体文件无效，会使用默认字体")
                        },
                        onClick = {
                            fontFileSelectLauncher.launch("")
                        }
                    )
                    XhuSettingsMenuLink(
                        title = { Text(text = "恢复默认字体") },
                        onClick = {
                            viewModel.setCustomFont(null)
                            toastString("字体恢复默认成功，重启应用后生效")
                        }
                    )
                }
                XhuSettingsGroup(title = {
                    Text(text = "通知设置")
                }) {
                    ConfigSettingsCheckbox(
                        config = ConfigStore::notifyCourse,
                        scope = scope,
                        icon = {
                            Icon(
                                painter = XhuIcons.notifyCourse,
                                contentDescription = null,
                                tint = XhuColor.Common.blackText,
                            )
                        },
                        title = { Text(text = "课程提醒") }
                    )
                    ConfigSettingsCheckbox(
                        config = ConfigStore::notifyExam,
                        scope = scope,
                        icon = {
                            Icon(
                                painter = XhuIcons.notifyExam,
                                contentDescription = null,
                                tint = XhuColor.Common.blackText,
                            )
                        },
                        title = { Text(text = "考试提醒") }
                    )
                    XhuSettingsMenuLink(
                        title = { Text(text = "提醒时间") },
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
                                    "将会在每天的 ${notifyTime!!.format(timeFormatter)} 提醒"
                                else
                                    "提醒功能已禁用"
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
                    XhuSettingsMenuLink(
                        title = { Text(text = "设置忽略电池优化") },
                        subtitle = {
                            Text(
                                text = "为了确保提醒功能正常，请将 $appName 添加到系统电池优化白名单中，点击即可前往设置"
                            )
                        },
                        onClick = {
                            if (isIgnoringBatteryOptimizations()) {
                                "已经在电池优化白名单中，无需重复设置".toast()
                            } else {
                                val intent =
                                    Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
                                intent.data = Uri.parse("package:$packageName")
                                startActivity(intent)
                            }
                        }
                    )
                }
                XhuSettingsGroup(title = {
                    Text(text = "诗词设置")
                }) {
                    PoemsSettingsCheckbox(
                        config = PoemsStore::disablePoems,
                        scope = scope,
                        title = { Text(text = "禁用今日诗词") }
                    ) {
                        scope.launch {
                            withContext(Dispatchers.IO) {
                                PoemsStore.disablePoems = it
                            }
                        }
                        "重启应用后生效".toast()
                    }
                    PoemsSettingsCheckbox(
                        config = PoemsStore::showPoemsTranslate,
                        scope = scope,
                        title = { Text(text = "显示诗词大意") }
                    ) {
                        scope.launch {
                            withContext(Dispatchers.IO) {
                                PoemsStore.showPoemsTranslate = it
                            }
                        }
                        "重启应用后生效".toast()
                    }
                    XhuSettingsMenuLink(
                        title = { Text(text = "重置Token") },
                        subtitle = { Text(text = "如果一直无法显示今日诗词，可能是缓存的Token出现了问题，点击此处可以进行重置") },
                        onClick = {
                            scope.launch {
                                viewModel.resetPoemsToken()
                                "重置成功，重启应用后生效".toast()
                            }
                        }
                    )
                }
                XhuSettingsGroup(title = {
                    Text(text = "其他设置")
                }) {
                    XhuSettingsMenuLink(
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
                                viewModel.checkUpdate()
                                toastString("检查更新完成")
                            }
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
                        config = ConfigStore::allowSendCrashReport,
                        scope = scope,
                        title = { Text(text = "发送错误报告") },
                        subtitle = {
                            Text(text = "这将帮助我们更快的发现并解决问题")
                        }
                    )
                    XhuSettingsMenuLink(
                        title = { },
                        subtitle = {
                            Text(
                                text = """
                              我们使用Visual Studio App Center （由Microsoft提供）。
                              
                              设备唯一标识符使用SSAID，在 Android 8.0（API 级别 26）及更高版本中，SSAID 提供了一个在由同一开发者签名密钥签名的应用之间通用的标识符。
                          """.trimIndent()
                            )
                        },
                        onClick = {},
                    )
                    XhuSettingsMenuLink(
                        icon = {
                            Icon(
                                painter = XhuIcons.qqGroup,
                                contentDescription = null,
                                tint = XhuColor.Common.blackText,
                            )
                        },
                        title = { Text(text = "点击加入『西瓜课表用户交流反馈』") },
                        onClick = {
                            joinQQGroup(this@SettingsActivity)
                        }
                    )
                }
                XhuSettingsGroup(title = {
                    Text(text = "重置设置")
                }) {
                    XhuSettingsMenuLink(
                        title = { Text(text = "重置背景图片") },
                        onClick = {
                            scope.launch {
                                setConfigStore { backgroundImage = null }
                                "背景图已重置".toast()
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
                            showUpdateLogState.show()
                        }
                    )
                    XhuSettingsMenuLink(
                        title = { Text(text = "西瓜课表官网") },
                        subtitle = {
                            Text(text = "点击访问 https://xgkb.mystery0.vip")
                        },
                        onClick = {
                            loadInBrowser("https://xgkb.mystery0.vip")
                        }
                    )
                    XhuSettingsMenuLink(
                        icon = {
                            Icon(
                                painter = XhuIcons.github,
                                contentDescription = null,
                                tint = XhuColor.Common.blackText,
                            )
                        },
                        title = { Text(text = "开源地址") },
                        subtitle = {
                            Text(text = "https://github.com/Mystery00/XhuTimetable")
                        },
                        onClick = {
                            loadInBrowser("https://github.com/Mystery00/XhuTimetable")
                        }
                    )
                    XhuSettingsMenuLink(
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
                    XhuSettingsMenuLink(
                        title = { Text(text = "版本名称") },
                        subtitle = {
                            Text(text = appVersionName)
                        },
                        onClick = {
                        }
                    )
                    XhuSettingsMenuLink(
                        title = { Text(text = "版本号") },
                        subtitle = {
                            Text(text = appVersionCode)
                        },
                        onClick = {
                            if (viewModel.clickVersion(3000L)) {
                                viewModel.enableDebugMode()
                                toastString("开发者模式已启用！")
                            }
                        }
                    )
                    XhuSettingsMenuLink(
                        title = { Text(text = "版本更新渠道") },
                        subtitle = {
                            Text(text = "重启应用后生效")
                        },
                        onClick = {
                            checkVersionChannelState.show()
                        }
                    )
                }
                XhuSettingsGroup(title = {
                    Text(text = "西瓜课表团队出品")
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
                            Text(text = "开发者选项")
                        },
                    ) {
                        XhuSettingsMenuLink(
                            title = { Text(text = "启用开发者模式") },
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
                        ConfigSettingsCheckbox(
                            config = ConfigStore::alwaysCrash,
                            scope = scope,
                            title = { Text(text = "始终显示崩溃信息") },
                            subtitle = {
                                Text(text = "不捕获全局异常")
                            }
                        )
                        XhuSettingsMenuLink(
                            title = { Text(text = "测试崩溃") },
                            onClick = {
                                scope.launch {
                                    throw TestCrashException()
                                }
                            },
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
                                Text(text = publicDeviceId)
                            },
                            onClick = {
                                scope.launch {
                                    clipboardManager.setPrimaryClip(
                                        ClipData.newPlainText(
                                            "设备id",
                                            publicDeviceId
                                        )
                                    )
                                }
                            },
                        )
                        val version by viewModel.version.collectAsState()
                        XhuSettingsMenuLink(
                            title = { Text(text = "新版本信息") },
                            subtitle = {
                                Text(text = version?.toString() ?: "无版本")
                            },
                            onClick = {
                            },
                        )
                        CacheSettingsCheckbox(
                            config = CacheStore::alwaysShowNewVersion,
                            scope = scope,
                            title = { Text(text = "始终显示新版本弹窗") }
                        )
                        XhuSettingsMenuLink(
                            title = { Text(text = "测试下载最新安装包") },
                            subtitle = {
                                Text(text = version?.apkSize?.formatFileSize() ?: "无版本")
                            },
                            onClick = {
                                if (version != null) {
                                    viewModel.downloadApk()
                                }
                            },
                        )
                        XhuSettingsMenuLink(
                            title = { Text(text = "测试下载最新增量包") },
                            subtitle = {
                                Text(text = version?.patchSize?.formatFileSize() ?: "无版本")
                            },
                            onClick = {
                                if (version != null) {
                                    viewModel.downloadPatch()
                                }
                            },
                        )
                        XhuSettingsMenuLink(
                            title = { Text(text = "NotifyWork 上一次执行时间") },
                            subtitle = {
                                Text(
                                    text = GlobalCacheStore.notifyWorkLastExecuteTime.formatChinaDateTime()
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
        BuildUpdateLogDialog(
            dialogState = showUpdateLogState
        )
        BuildVersionChannelDialog(
            dialogState = checkVersionChannelState,
            initChannel = versionChannel,
        )
        ShowCheckUpdateDialog()
    }

    @Composable
    private fun BuildNightModeSelector(
        dialogState: MaterialDialogState,
        initNightMode: NightMode,
    ) {
        val list = NightMode.selectList()
        var selectedMode = list.indexOf(initNightMode)
        if (selectedMode == -1) selectedMode = 0
        MaterialDialog(
            dialogState = dialogState,
            buttons = {
                positiveButton("确定") {
                    viewModel.updateNightMode(list[selectedMode])
                }
                negativeButton("取消")
            }) {
            title("更改主题")
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
                positiveButton("确定") {
                    viewModel.updateNotifyTime(selectedTime)
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
    private fun BuildUpdateLogDialog(dialogState: MaterialDialogState) {
        if (!dialogState.showing) {
            return
        }
        AlertDialog(
            onDismissRequest = {
                dialogState.hide()
            },
            title = {
                Text(
                    text = "$appVersionName 更新日志",
                    fontWeight = FontWeight.Bold,
                )
            },
            text = {
                Text(text = updateLogArray.joinToString("\n"))
            },
            confirmButton = {
                TextButton(onClick = {
                    dialogState.hide()
                }) {
                    Text(text = "关闭")
                }
            })
    }

    @Composable
    private fun BuildVersionChannelDialog(
        dialogState: MaterialDialogState,
        initChannel: VersionChannel,
    ) {
        val list = VersionChannel.selectList()
        var selectedMode = list.indexOf(initChannel)
        if (selectedMode == -1) selectedMode = 0
        MaterialDialog(
            dialogState = dialogState,
            buttons = {
                positiveButton("确定") {
                    viewModel.updateVersionChannel(list[selectedMode])
                }
                negativeButton("取消")
            }) {
            title("修改更新渠道")
            listItemsSingleChoice(
                list = list.map { it.title },
                initialSelection = selectedMode,
            ) {
                selectedMode = it
            }
        }
    }

    @Composable
    private fun ShowCheckUpdateDialog() {
        val version by viewModel.version.collectAsState()
        val newVersion = version ?: return
        if (newVersion == ClientVersion.EMPTY) {
            return
        }
        val scope = rememberCoroutineScope()
        //需要提示版本更新
        CheckUpdate(
            version = newVersion,
            onDownload = {
                if (it) {
                    viewModel.downloadApk()
                } else {
                    viewModel.downloadPatch()
                }
            },
            onIgnore = {
            },
            onClose = {
                scope.launch {
                    StartRepo.version.emit(ClientVersion.EMPTY)
                }
            },
        )
    }

    override fun onStart() {
        super.onStart()
        pushDynamicShortcuts<SettingsActivity>(iconResId = R.drawable.ic_settings)
    }
}

@Composable
private fun TeamItem(
    painter: Painter,
    title: String,
    subTitle: String,
    onClick: () -> Unit = {},
) {
    XhuSettingsMenuLink(
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