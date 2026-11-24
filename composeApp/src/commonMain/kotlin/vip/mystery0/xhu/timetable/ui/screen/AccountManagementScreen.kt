package vip.mystery0.xhu.timetable.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Badge
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import multiplatform.network.cmptoast.showToast
import org.koin.compose.viewmodel.koinViewModel
import vip.mystery0.xhu.timetable.base.HandleErrorMessage
import vip.mystery0.xhu.timetable.config.store.ConfigStore
import vip.mystery0.xhu.timetable.config.store.EventBus
import vip.mystery0.xhu.timetable.config.store.GlobalConfigStore
import vip.mystery0.xhu.timetable.model.AccountTitleTemplate
import vip.mystery0.xhu.timetable.model.CustomAccountTitle
import vip.mystery0.xhu.timetable.model.Gender
import vip.mystery0.xhu.timetable.model.event.EventType
import vip.mystery0.xhu.timetable.ui.component.dialog.XhuConfirmDialog
import vip.mystery0.xhu.timetable.ui.component.dialog.XhuTemplateInputDialog
import vip.mystery0.xhu.timetable.ui.component.dialog.rememberXhuDialogState
import vip.mystery0.xhu.timetable.ui.component.preference.ConfigSettingsCheckbox
import vip.mystery0.xhu.timetable.ui.component.preference.XhuSettingsGroup
import vip.mystery0.xhu.timetable.ui.component.preference.XhuSettingsMenuLink
import vip.mystery0.xhu.timetable.ui.navigation.LocalNavController
import vip.mystery0.xhu.timetable.ui.navigation.RouteLogin
import vip.mystery0.xhu.timetable.ui.navigation.navigateAndSave
import vip.mystery0.xhu.timetable.ui.theme.ProfileImages
import vip.mystery0.xhu.timetable.ui.theme.XhuColor
import vip.mystery0.xhu.timetable.ui.theme.XhuIcons
import vip.mystery0.xhu.timetable.viewmodel.AccountManagementViewModel
import vip.mystery0.xhu.timetable.viewmodel.UserItem

@Composable
fun AccountManagementScreen() {
    val viewModel = koinViewModel<AccountManagementViewModel>()
    val navController = LocalNavController.current

    val loggedUserList by viewModel.loggedUserList.collectAsState()
    val customAccountTitle by viewModel.customAccountTitle.collectAsState()
    var multiAccountEnabled by remember { mutableStateOf(GlobalConfigStore.multiAccountMode) }

    val todayTemplateDialog = rememberXhuDialogState()
    val weekTemplateDialog = rememberXhuDialogState()
    val logoutConfirmDialog = rememberXhuDialogState()
    var pendingLogoutUser by remember { mutableStateOf<UserItem?>(null) }

    LaunchedEffect(Unit) {
        viewModel.init()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "账号管理",
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
        floatingActionButton = {
            ExtendedFloatingActionButton(
                text = { Text(text = "登录其他账号") },
                onClick = {
                    navController.navigateAndSave(RouteLogin(fromAccountManager = true))
                },
                icon = {
                    Icon(
                        imageVector = Icons.Rounded.Add,
                        contentDescription = "登录其他账号",
                    )
                },
            )
        },
        floatingActionButtonPosition = FabPosition.Center,
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 88.dp),
        ) {
            XhuSettingsGroup(title = { Text("多账号显示") }) {
                ConfigSettingsCheckbox(
                    config = ConfigStore::multiAccountMode,
                    icon = {
                        Icon(
                            painter = XhuIcons.multiUser,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface,
                        )
                    },
                    title = { Text("多账号模式") },
                    subtitle = { Text("同时展示所有已登录账号的课程安排") },
                ) { enabled ->
                    multiAccountEnabled = enabled
                    EventBus.post(EventType.MULTI_MODE_CHANGED)
                }

                AnimatedVisibility(
                    visible = multiAccountEnabled,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut(),
                ) {
                    Column {
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
                        )
                        XhuSettingsMenuLink(
                            icon = {
                                Icon(
                                    imageVector = Icons.Rounded.Badge,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            },
                            title = { Text("今日课程账号信息模板") },
                            subtitle = { Text("当前：${customAccountTitle.todayTemplate}") },
                            onClick = { todayTemplateDialog.show() },
                        )
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
                        )
                        XhuSettingsMenuLink(
                            icon = {
                                Icon(
                                    imageVector = Icons.Rounded.Badge,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            },
                            title = { Text("本周课程账号信息模板") },
                            subtitle = { Text("当前：${customAccountTitle.weekTemplate}") },
                            onClick = { weekTemplateDialog.show() },
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = "已登录账号 (${loggedUserList.size})",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        text = "点击卡片切换主账号",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                loggedUserList.forEach { userItem ->
                    AccountUserCard(
                        user = userItem,
                        onClick = {
                            if (!userItem.main) {
                                viewModel.changeMainUser(userItem.studentId)
                            }
                        },
                        onRefreshClick = {
                            viewModel.reloadUserInfo(userItem.studentId)
                            showToast("正在刷新用户信息…")
                        },
                        onLogoutClick = {
                            pendingLogoutUser = userItem
                            logoutConfirmDialog.show()
                        },
                    )
                }
            }
        }
    }

    val accountTokens = listOf(
        "学号" to "{${AccountTitleTemplate.STUDENT_NO.tpl}}",
        "姓名" to "{${AccountTitleTemplate.NAME.tpl}}",
        "昵称" to "{${AccountTitleTemplate.NICK_NAME.tpl}}",
    )

    XhuTemplateInputDialog(
        state = todayTemplateDialog,
        title = "今日课程账号模板",
        initialValue = customAccountTitle.todayTemplate,
        resetValue = CustomAccountTitle.DEFAULT.todayTemplate,
        quickTokens = accountTokens,
        onConfirm = { newValue ->
            viewModel.updateAccountTitleTemplate(customAccountTitle.copy(todayTemplate = newValue))
        },
    )

    XhuTemplateInputDialog(
        state = weekTemplateDialog,
        title = "本周课程账号模板",
        initialValue = customAccountTitle.weekTemplate,
        resetValue = CustomAccountTitle.DEFAULT.weekTemplate,
        quickTokens = accountTokens,
        onConfirm = { newValue ->
            viewModel.updateAccountTitleTemplate(customAccountTitle.copy(weekTemplate = newValue))
        },
    )

    pendingLogoutUser?.let { targetUser ->
        XhuConfirmDialog(
            state = logoutConfirmDialog,
            title = "退出登录",
            message = "确认退出账号「${targetUser.userName} (${targetUser.studentId})」吗？退出后将清除该账号的本地登录态。",
            icon = { Icon(Icons.AutoMirrored.Rounded.Logout, contentDescription = null) },
            confirmText = "确认退出",
            isDestructive = true,
            onConfirm = {
                viewModel.logoutUser(targetUser.studentId)
                pendingLogoutUser = null
            },
        )
    }

    HandleErrorMessage(flow = viewModel.errorMessage)
}

@Composable
private fun AccountUserCard(
    user: UserItem,
    onClick: () -> Unit,
    onRefreshClick: () -> Unit,
    onLogoutClick: () -> Unit,
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = XhuColor.cardBackground),
        border = if (user.main) {
            BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
        } else {
            BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))
        },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Image(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(14.dp)),
                    painter = ProfileImages.hash(
                        user.userName,
                        user.gender == Gender.MALE
                    ),
                    contentDescription = null,
                )
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(3.dp),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            text = user.userName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        if (user.main) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.primaryContainer,
                            ) {
                                Text(
                                    text = "当前主账号",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
                                )
                            }
                        }
                    }
                    Text(
                        text = user.studentId,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                // 显式刷新与退出按钮（触控区均 >= 40dp，防止误触）
                FilledTonalIconButton(
                    onClick = onRefreshClick,
                    modifier = Modifier.size(40.dp),
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Refresh,
                        contentDescription = "强制刷新用户信息",
                        modifier = Modifier.size(18.dp),
                    )
                }
                FilledTonalIconButton(
                    onClick = onLogoutClick,
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer,
                    ),
                    modifier = Modifier.size(40.dp),
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.Logout,
                        contentDescription = "退出登录",
                        modifier = Modifier.size(18.dp),
                    )
                }
            }

            val meta = buildList {
                add("${user.xhuGrade}级")
                if (user.college.isNotBlank()) add(user.college)
                if (user.majorName.isNotBlank()) add(user.majorName)
                if (user.majorDirection.isNotBlank()) add(user.majorDirection)
            }
            if (meta.isNotEmpty()) {
                Text(
                    text = meta.joinToString(" · "),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
