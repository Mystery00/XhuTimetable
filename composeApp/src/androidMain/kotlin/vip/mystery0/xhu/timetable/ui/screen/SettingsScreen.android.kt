package vip.mystery0.xhu.timetable.ui.screen

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.PowerManager
import android.provider.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri
import com.maxkeppeker.sheets.core.models.base.Header
import com.maxkeppeker.sheets.core.models.base.UseCaseState
import com.maxkeppeker.sheets.core.models.base.rememberUseCaseState
import com.maxkeppeler.sheets.clock.ClockDialog
import com.maxkeppeler.sheets.clock.models.ClockConfig
import com.maxkeppeler.sheets.clock.models.ClockSelection
import kotlinx.datetime.LocalTime
import kotlinx.datetime.format
import org.koin.compose.viewmodel.koinViewModel
import vip.mystery0.xhu.timetable.base.appName
import vip.mystery0.xhu.timetable.base.packageName
import vip.mystery0.xhu.timetable.config.store.CacheStore
import vip.mystery0.xhu.timetable.config.store.ConfigStore
import vip.mystery0.xhu.timetable.config.store.GlobalCacheStore
import vip.mystery0.xhu.timetable.config.toast.showShortToast
import vip.mystery0.xhu.timetable.context
import vip.mystery0.xhu.timetable.model.entity.VersionChannel
import vip.mystery0.xhu.timetable.ui.component.ShowSingleSelectDialog
import vip.mystery0.xhu.timetable.ui.component.ShowUpdateDialog
import vip.mystery0.xhu.timetable.ui.component.preference.CacheSettingsCheckbox
import vip.mystery0.xhu.timetable.ui.component.preference.ConfigSettingsCheckbox
import vip.mystery0.xhu.timetable.ui.component.preference.XhuActionSettingsCheckbox
import vip.mystery0.xhu.timetable.ui.component.preference.XhuSettingsGroup
import vip.mystery0.xhu.timetable.ui.component.preference.XhuSettingsMenuLink
import vip.mystery0.xhu.timetable.ui.theme.XhuIcons
import vip.mystery0.xhu.timetable.utils.asLocalDateTime
import vip.mystery0.xhu.timetable.utils.chinaDateTime
import vip.mystery0.xhu.timetable.utils.formatFileSize
import vip.mystery0.xhu.timetable.utils.now
import vip.mystery0.xhu.timetable.utils.timeFormatter
import vip.mystery0.xhu.timetable.viewmodel.PlatformSettingsViewModel

@SuppressLint("BatteryLife")
@Composable
actual fun NotifySettings() {
    val viewModel = koinViewModel<PlatformSettingsViewModel>()

    val context = LocalContext.current

    val notifyTime by viewModel.notifyTimeData.collectAsState()

    val scope = rememberCoroutineScope()
    val showNotifyTimeState = rememberUseCaseState()

    LaunchedEffect(Unit) {
        viewModel.init()
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
                    tint = MaterialTheme.colorScheme.onSurface,
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
                    tint = MaterialTheme.colorScheme.onSurface,
                )
            },
            title = { Text(text = "考试提醒") }
        )
        XhuActionSettingsCheckbox(
            title = { Text(text = "提醒时间") },
            icon = {
                Icon(
                    painter = XhuIcons.notifyTime,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface,
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
            onCheckedChange = {
                viewModel.updateNotifyTime(null)
            },
            checkboxEnabled = notifyTime != null,
            checked = notifyTime != null,
            onClick = {
                showNotifyTimeState.show()
            }
        )
        XhuSettingsMenuLink(
            title = { Text(text = "设置忽略电池优化") },
            subtitle = {
                Text(
                    text = "为了确保提醒功能正常，请将 ${appName()} 添加到系统电池优化白名单中，点击即可前往设置"
                )
            },
            onClick = {
                if (isIgnoringBatteryOptimizations()) {
                    showShortToast("已经在电池优化白名单中，无需重复设置")
                } else {
                    val intent =
                        Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
                    intent.data = "package:${packageName()}".toUri()
                    context.startActivity(intent)
                }
            }
        )
    }

    BuildTimeSelector(
        useCaseState = showNotifyTimeState,
        initTime = notifyTime ?: LocalTime.now(),
        onSelect = {
            viewModel.updateNotifyTime(it)
        }
    )
}

@Composable
private fun BuildTimeSelector(
    useCaseState: UseCaseState,
    initTime: LocalTime,
    onSelect: (LocalTime) -> Unit
) {
    ClockDialog(
        header = Header.Default(
            title = "请选择时间",
        ),
        state = useCaseState,
        selection = ClockSelection.HoursMinutes { hour, minutes ->
            onSelect(LocalTime(hour, minutes))
        },
        config = ClockConfig(
            defaultTime = initTime,
            is24HourFormat = true,
        )
    )
}

private fun isIgnoringBatteryOptimizations(): Boolean {
    var isIgnoring = false
    val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager?
    if (powerManager != null) {
        isIgnoring = powerManager.isIgnoringBatteryOptimizations(packageName())
    }
    return isIgnoring
}

@Composable
actual fun UpdateSettings() {
    val viewModel = koinViewModel<PlatformSettingsViewModel>()

    val versionChannel by viewModel.versionChannel.collectAsState()

    val checkVersionChannelState = rememberUseCaseState()

    XhuSettingsMenuLink(
        icon = {
            Icon(
                painter = XhuIcons.checkUpdate,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface,
            )
        },
        title = { Text(text = "检查更新") },
        onClick = {
            viewModel.checkUpdate(false)
            showShortToast("检查更新完成")
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

    BuildVersionChannelDialog(
        useCaseState = checkVersionChannelState,
        initChannel = versionChannel,
        onSelect = {
            viewModel.updateVersionChannel(it)
        }
    )
    ShowUpdateDialog()
}

@Composable
private fun BuildVersionChannelDialog(
    useCaseState: UseCaseState,
    initChannel: VersionChannel,
    onSelect: (VersionChannel) -> Unit
) {
    val list = VersionChannel.selectList()
    var selectedMode = list.indexOf(initChannel)
    if (selectedMode == -1) selectedMode = 0

    ShowSingleSelectDialog(
        dialogTitle = "修改更新渠道",
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
actual fun DeveloperSettings() {
    val viewModel = koinViewModel<PlatformSettingsViewModel>()
    XhuSettingsMenuLink(
        icon = {
            Icon(
                painter = XhuIcons.checkUpdate,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface,
            )
        },
        title = { Text(text = "直接检查测试版更新") },
        onClick = {
            viewModel.checkUpdate(true)
            showShortToast("检查更新完成")
        }
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
                text = GlobalCacheStore.notifyWorkLastExecuteTime.asLocalDateTime()
                    .format(chinaDateTime)
            )
        },
        onClick = {
        }
    )
}