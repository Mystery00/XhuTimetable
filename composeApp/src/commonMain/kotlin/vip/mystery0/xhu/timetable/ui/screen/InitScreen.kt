package vip.mystery0.xhu.timetable.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PrivacyTip
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import io.github.vinceglb.filekit.absolutePath
import kotlinx.datetime.LocalDate
import kotlinx.datetime.format
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel
import vip.mystery0.xhu.timetable.base.HandleErrorMessage
import vip.mystery0.xhu.timetable.base.appName
import vip.mystery0.xhu.timetable.killCurrentProcess
import vip.mystery0.xhu.timetable.module.PRIVACY_URL
import vip.mystery0.xhu.timetable.ui.component.dialog.XhuDialog
import vip.mystery0.xhu.timetable.ui.component.dialog.rememberXhuDialogState
import vip.mystery0.xhu.timetable.ui.navigation.LocalNavController
import vip.mystery0.xhu.timetable.ui.navigation.RouteInit
import vip.mystery0.xhu.timetable.ui.navigation.RouteLogin
import vip.mystery0.xhu.timetable.ui.navigation.RouteMain
import vip.mystery0.xhu.timetable.ui.navigation.RouteSplashImage
import vip.mystery0.xhu.timetable.ui.navigation.replaceTo
import vip.mystery0.xhu.timetable.ui.theme.XhuIcons
import vip.mystery0.xhu.timetable.utils.dateFormatter
import vip.mystery0.xhu.timetable.utils.formatWeekString
import vip.mystery0.xhu.timetable.viewmodel.StarterViewModel
import xhutimetable.composeapp.generated.resources.Res
import xhutimetable.composeapp.generated.resources.ic_app_icon_o

@Composable
fun InitScreen() {
    val viewModel = koinViewModel<StarterViewModel>()
    val navController = LocalNavController.current

    val allowPrivacy by viewModel.allowPrivacy.collectAsState()
    val readyState by viewModel.readyState.collectAsState()

    val privacyDialogState = rememberXhuDialogState(initialVisible = false)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.align(Alignment.Center),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Surface(
                shape = RoundedCornerShape(32.dp),
                tonalElevation = 4.dp,
                shadowElevation = 6.dp,
                modifier = Modifier.size(104.dp),
            ) {
                Image(
                    painter = painterResource(Res.drawable.ic_app_icon_o),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(32.dp))
                )
            }
            Text(
                text = appName(),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(modifier = Modifier.height(24.dp))
            LoadingIndicator()
            Text(
                text = "正在准备课表数据…",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }

    // 自研跨平台隐私政策对话框：主次按钮层级清晰
    XhuDialog(
        state = privacyDialogState,
        title = "隐私政策提示",
        icon = { Icon(Icons.Rounded.PrivacyTip, contentDescription = null) },
        dismissOnBackPress = false,
        dismissOnClickOutside = false,
        dismissButtonText = "不同意并退出",
        onDismissButtonClick = { killCurrentProcess() },
        confirmButtonText = "同意并继续",
        onConfirmButtonClick = {
            privacyDialogState.hide()
            viewModel.allowPrivacy()
        },
    ) {
        val linkColor = MaterialTheme.colorScheme.primary
        Text(
            text = buildAnnotatedString {
                append("本应用尊重并保护所有用户的个人隐私权。为了向你提供课程表同步、成绩与考试查询等服务，本应用会按照隐私政策的规定使用必要信息。请仔细阅读 ")
                withLink(
                    LinkAnnotation.Url(
                        url = PRIVACY_URL,
                        styles = TextLinkStyles(
                            style = SpanStyle(
                                color = linkColor,
                                fontWeight = FontWeight.SemiBold,
                                textDecoration = TextDecoration.Underline,
                            )
                        )
                    )
                ) {
                    append("《隐私政策》")
                }
                append("。")
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }

    HandleErrorMessage(errorMessage = readyState.errorMessage) {}

    LaunchedEffect(allowPrivacy) {
        if (allowPrivacy) {
            privacyDialogState.hide()
            viewModel.doInitAndReady()
        } else {
            privacyDialogState.show()
        }
    }

    readyState.termStartChangeAlert?.let { alert ->
        fun formatTermDate(date: LocalDate): String =
            "${date.format(dateFormatter)}（${date.dayOfWeek.formatWeekString()}）"

        AlertDialog(
            onDismissRequest = {},
            properties = DialogProperties(
                dismissOnBackPress = false,
                dismissOnClickOutside = false,
            ),
            icon = {
                Icon(
                    painter = XhuIcons.customStartTime,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
            },
            title = {
                Text(
                    text = "开学时间已更新",
                    style = MaterialTheme.typography.titleLarge,
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "云端开学日期发生变更，与您当前自定义设置不一致：",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "最新云端：${formatTermDate(alert.newServerDate)}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                            )
                            Text(
                                text = "原云端：${formatTermDate(alert.oldServerDate)}（较原云端 ${alert.serverChangeDescription}）",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 2.dp),
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                            )
                            Text(
                                text = "当前自定义：${formatTermDate(alert.customDate)}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Text(
                                text = "（${alert.customDiffDescription}）",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                    Text(
                        text = "选择“同步云端”将清除自定义设置，后续自动跟随学校安排；选择“保留自定义”将继续按当前设定的日期计算教学周。",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            },
            confirmButton = {
                Button(onClick = { viewModel.syncTermStartDate() }) {
                    Text(text = "同步云端")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.keepCustomTermStartDate() }) {
                    Text(text = "保留自定义")
                }
            },
        )
    }

    LaunchedEffect(readyState.canNavigate, readyState.isLogin, readyState.splashFile, readyState.splashId) {
        if (readyState.canNavigate) {
            if (!readyState.isLogin) {
                navController.replaceTo<RouteInit>(RouteLogin(false))
            } else if (readyState.splashFile == null || readyState.splashId == null) {
                navController.replaceTo<RouteInit>(RouteMain)
            } else {
                val splashFilePath = readyState.splashFile!!.absolutePath()
                navController.replaceTo<RouteInit>(RouteSplashImage(splashFilePath, readyState.splashId!!))
            }
        }
    }
}
