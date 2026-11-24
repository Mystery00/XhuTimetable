package vip.mystery0.xhu.timetable.ui.screen

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.PowerManager
import android.provider.Settings
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BatteryChargingFull
import androidx.compose.material.icons.rounded.SystemUpdateAlt
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
import vip.mystery0.xhu.timetable.ui.component.ShowUpdateDialog
import vip.mystery0.xhu.timetable.ui.component.dialog.XhuSingleSelectDialog
import vip.mystery0.xhu.timetable.ui.component.dialog.XhuTimePickerDialog
import vip.mystery0.xhu.timetable.ui.component.dialog.rememberXhuDialogState
import vip.mystery0.xhu.timetable.ui.component.preference.CacheSettingsCheckbox
import vip.mystery0.xhu.timetable.ui.component.preference.ConfigSettingsCheckbox
import vip.mystery0.xhu.timetable.ui.component.preference.XhuActionSettingsCheckbox
import vip.mystery0.xhu.timetable.ui.component.preference.XhuSettingsGroup
import vip.mystery0.xhu.timetable.ui.component.preference.XhuSettingsMenuLink
import vip.mystery0.xhu.timetable.ui.theme.XhuIcons
import vip.mystery0.xhu.timetable.utils.asLocalDateTime
import vip.mystery0.xhu.timetable.utils.chinaDateTime
import vip.mystery0.xhu.timetable.utils.formatFileSize
import vip.mystery0.xhu.timetable.utils.timeFormatter
import vip.mystery0.xhu.timetable.viewmodel.PlatformSettingsViewModel

@SuppressLint("BatteryLife")
@Composable
actual fun NotifySettings() {
    val viewModel = koinViewModel<PlatformSettingsViewModel>()
    val context = LocalContext.current
    val notifyTime by viewModel.notifyTimeData.collectAsState()
    val scope = rememberCoroutineScope()
    val notifyTimeDialog = rememberXhuDialogState()

    LaunchedEffect(Unit) {
        viewModel.init()
    }

    XhuSettingsGroup(title = { Text("通知提醒") }) {
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
            title = { Text("课程提醒") }
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
            title = { Text("考试提醒") }
        )
        XhuActionSettingsCheckbox(
            title = { Text("每日提醒时间") },
            icon = {
                Icon(
                    painter = XhuIcons.notifyTime,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface,
                )
            },
            subtitle = {
                Text(
                    text = if (notifyTime != null) {
                        "将在每天 ${notifyTime!!.format(timeFormatter)} 发送提醒"
                    } else {
                        "已关闭，点击开关即可开启并设定时间"
                    }
                )
            },
            onCheckedChange = { checked ->
                if (checked) {
                    notifyTimeDialog.show()
                } else {
                    viewModel.updateNotifyTime(null)
                }
            },
            checkboxEnabled = true,
            checked = notifyTime != null,
            onClick = { notifyTimeDialog.show() }
        )
        XhuSettingsMenuLink(
            icon = {
                Icon(
                    imageVector = Icons.Rounded.BatteryChargingFull,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface,
                )
            },
            title = { Text("忽略电池优化") },
            subtitle = { Text("将 ${appName()} 加入白名单，避免后台提醒被系统拦截") },
            onClick = {
                if (isIgnoringBatteryOptimizations()) {
                    showShortToast("已经在电池优化白名单中，无需重复设置")
                } else {
                    val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
                    intent.data = "package:${packageName()}".toUri()
                    context.startActivity(intent)
                }
            }
        )
    }

    XhuTimePickerDialog(
        state = notifyTimeDialog,
        title = "选择每日提醒时间",
        initialTime = notifyTime ?: LocalTime(20, 30),
        extraButtonText = if (notifyTime != null) "关闭提醒" else null,
        onExtraButtonClick = { viewModel.updateNotifyTime(null) },
        onSelectTime = { viewModel.updateNotifyTime(it) },
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
    val channelDialog = rememberXhuDialogState()

    XhuSettingsMenuLink(
        icon = {
            Icon(
                painter = XhuIcons.checkUpdate,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface,
            )
        },
        title = { Text("检查更新") },
        onClick = {
            viewModel.checkUpdate(false)
            showShortToast("正在检查更新…")
        }
    )
    XhuSettingsMenuLink(
        icon = {
            Icon(
                imageVector = Icons.Rounded.SystemUpdateAlt,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface,
            )
        },
        title = { Text("版本更新渠道") },
        subtitle = { Text("当前：${versionChannel.title}（更改后重启生效）") },
        onClick = { channelDialog.show() }
    )

    val list = VersionChannel.selectList()
    XhuSingleSelectDialog(
        state = channelDialog,
        title = "修改更新渠道",
        options = list,
        selectedIndex = list.indexOf(versionChannel).coerceAtLeast(0),
        itemLabel = { it.title },
        onSelect = { _, select -> viewModel.updateVersionChannel(select) },
    )
    ShowUpdateDialog()
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
        title = { Text("直接检查测试版更新") },
        onClick = {
            viewModel.checkUpdate(true)
            showShortToast("检查更新完成")
        }
    )
    val version by viewModel.version.collectAsState()
    XhuSettingsMenuLink(
        title = { Text("新版本信息") },
        subtitle = { Text(version?.toString() ?: "无版本") },
        onClick = {},
    )
    CacheSettingsCheckbox(
        config = CacheStore::alwaysShowNewVersion,
        title = { Text("始终显示新版本弹窗") }
    )
    XhuSettingsMenuLink(
        title = { Text("测试下载最新安装包") },
        subtitle = { Text(version?.apkSize?.formatFileSize() ?: "无版本") },
        onClick = {
            if (version != null) viewModel.downloadApk()
        },
    )
    XhuSettingsMenuLink(
        title = { Text("测试下载最新增量包") },
        subtitle = { Text(version?.patchSize?.formatFileSize() ?: "无版本") },
        onClick = {
            if (version != null) viewModel.downloadPatch()
        },
    )
    XhuSettingsMenuLink(
        title = { Text("NotifyWork 上一次执行时间") },
        subtitle = {
            Text(
                GlobalCacheStore.notifyWorkLastExecuteTime.asLocalDateTime()
                    .format(chinaDateTime)
            )
        },
        onClick = {}
    )
}
