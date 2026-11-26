package vip.mystery0.xhu.timetable.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Badge
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.PermMedia
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.SettingsBackupRestore
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.attafitamim.krop.core.crop.AspectRatio
import com.attafitamim.krop.core.crop.CropError
import com.attafitamim.krop.core.crop.CropResult
import com.attafitamim.krop.core.crop.crop
import com.attafitamim.krop.core.crop.cropperStyle
import com.attafitamim.krop.core.crop.rememberImageCropper
import com.attafitamim.krop.filekit.ImageFormat
import com.attafitamim.krop.filekit.encodeToByteArray
import com.attafitamim.krop.filekit.toImageSrc
import com.attafitamim.krop.ui.ImageCropperDialog
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import kotlinx.coroutines.Dispatchers
import multiplatform.network.cmptoast.showToast
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel
import vip.mystery0.xhu.timetable.base.HandleErrorMessage
import vip.mystery0.xhu.timetable.config.coroutine.safeLaunch
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
import vip.mystery0.xhu.timetable.ui.theme.ColorPool
import vip.mystery0.xhu.timetable.ui.theme.CourseColorHelper
import vip.mystery0.xhu.timetable.ui.theme.ProfileImages
import vip.mystery0.xhu.timetable.ui.theme.UserAvatarImage
import vip.mystery0.xhu.timetable.ui.theme.XhuColor
import vip.mystery0.xhu.timetable.ui.theme.XhuIcons
import vip.mystery0.xhu.timetable.viewmodel.AccountManagementViewModel
import vip.mystery0.xhu.timetable.viewmodel.UserItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountManagementScreen() {
    val viewModel = koinViewModel<AccountManagementViewModel>()
    val navController = LocalNavController.current
    val scope = rememberCoroutineScope()

    val loggedUserList by viewModel.loggedUserList.collectAsState()
    val customAccountTitle by viewModel.customAccountTitle.collectAsState()
    val customAvatar by viewModel.customAvatar.collectAsState()
    var multiAccountEnabled by remember { mutableStateOf(GlobalConfigStore.multiAccountMode) }
    var showAvatarSheet by remember { mutableStateOf(false) }

    val imageCropper = rememberImageCropper()
    val pickerLauncher = rememberFilePickerLauncher(FileKitType.Image) { imageFile ->
        val image = imageFile ?: return@rememberFilePickerLauncher
        scope.safeLaunch(Dispatchers.Main) {
            when (val result = imageCropper.crop(image.toImageSrc())) {
                CropResult.Cancelled -> {}
                is CropError -> showToast("图片裁剪失败")
                is CropResult.Success -> {
                    val bytes = result.bitmap.encodeToByteArray(ImageFormat.JPEG)
                    viewModel.saveCustomAvatar(bytes)
                    showToast("自定义头像已更新")
                }
            }
        }
    }

    val todayTemplateDialog = rememberXhuDialogState()
    val weekTemplateDialog = rememberXhuDialogState()
    val logoutConfirmDialog = rememberXhuDialogState()
    val resetAvatarConfirmDialog = rememberXhuDialogState()
    var pendingLogoutUser by remember { mutableStateOf<UserItem?>(null) }

    val mainUser = loggedUserList.firstOrNull { it.main } ?: loggedUserList.firstOrNull()

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
                .padding(bottom = 88.dp + paddingValues.calculateBottomPadding()),
        ) {
            XhuSettingsGroup(title = { Text("个性头像") }) {
                val avatarStatusText = when {
                    customAvatar.startsWith(ProfileImages.FILE_PREFIX) -> "当前：相册自定义头像"
                    customAvatar.startsWith(ProfileImages.BUILTIN_PREFIX) -> {
                        val builtin = ProfileImages.findBuiltin(
                            customAvatar.removePrefix(ProfileImages.BUILTIN_PREFIX)
                        )
                        "当前：内置头像 · ${builtin?.label ?: "已选择"}"
                    }

                    else -> "当前：默认头像（按账号自动匹配）"
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showAvatarSheet = true }
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    UserAvatarImage(
                        userName = mainUser?.userName,
                        isMale = mainUser?.gender == Gender.MALE,
                        contentDescription = "当前头像",
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f),
                                shape = RoundedCornerShape(16.dp),
                            ),
                    )
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(3.dp),
                    ) {
                        Text(
                            text = "本地头像",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = avatarStatusText,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = "仅本地生效，切换账号保持不变",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.72f),
                        )
                    }
                    FilledTonalButton(
                        onClick = { showAvatarSheet = true },
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                    ) {
                        Text(
                            text = "更换",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }

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

    if (showAvatarSheet) {
        AvatarPickerBottomSheet(
            currentAvatar = customAvatar,
            onSelectBuiltin = { key ->
                viewModel.selectBuiltinAvatar(key)
                showToast("头像已更新")
                showAvatarSheet = false
            },
            onPickFromGallery = {
                showAvatarSheet = false
                pickerLauncher.launch()
            },
            onResetDefault = {
                resetAvatarConfirmDialog.show()
            },
            onDismiss = { showAvatarSheet = false },
        )
    }

    XhuConfirmDialog(
        state = resetAvatarConfirmDialog,
        title = "恢复默认头像",
        message = "确认清除当前自定义头像并恢复为按账号自动匹配的默认头像吗？",
        confirmText = "恢复默认",
        onConfirm = {
            viewModel.resetCustomAvatar()
            showToast("已恢复默认头像")
            showAvatarSheet = false
        },
    )

    val cropState = imageCropper.cropState
    if (cropState != null) {
        ImageCropperDialog(
            state = cropState,
            style = cropperStyle(
                shapes = emptyList(),
                aspects = listOf(AspectRatio(1, 1)),
            ),
        )
    }

    HandleErrorMessage(flow = viewModel.errorMessage)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AvatarPickerBottomSheet(
    currentAvatar: String,
    onSelectBuiltin: (String) -> Unit,
    onPickFromGallery: () -> Unit,
    onResetDefault: () -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val selectedBuiltinKey = currentAvatar
        .takeIf { it.startsWith(ProfileImages.BUILTIN_PREFIX) }
        ?.removePrefix(ProfileImages.BUILTIN_PREFIX)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "选择本地头像",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = "仅保存在本地设备，不上传服务端，切换账号后保持不变",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                ProfileImages.builtinAvatars.chunked(4).forEach { rowAvatars ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        rowAvatars.forEach { avatar ->
                            val isSelected = selectedBuiltinKey == avatar.key
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(14.dp))
                                    .clickable { onSelectBuiltin(avatar.key) }
                                    .padding(vertical = 2.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .aspectRatio(1f)
                                        .clip(RoundedCornerShape(16.dp))
                                        .border(
                                            width = if (isSelected) 2.5.dp else 0.8.dp,
                                            color = if (isSelected) {
                                                MaterialTheme.colorScheme.primary
                                            } else {
                                                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                            },
                                            shape = RoundedCornerShape(16.dp),
                                        ),
                                ) {
                                    Image(
                                        painter = painterResource(avatar.resource),
                                        contentDescription = avatar.label,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize(),
                                    )
                                    if (isSelected) {
                                        Box(
                                            modifier = Modifier
                                                .align(Alignment.BottomEnd)
                                                .padding(4.dp)
                                                .size(22.dp)
                                                .clip(CircleShape)
                                                .background(MaterialTheme.colorScheme.primary),
                                            contentAlignment = Alignment.Center,
                                        ) {
                                            Icon(
                                                imageVector = Icons.Rounded.Check,
                                                contentDescription = "已选中",
                                                tint = MaterialTheme.colorScheme.onPrimary,
                                                modifier = Modifier.size(14.dp),
                                            )
                                        }
                                    }
                                }
                                Text(
                                    text = avatar.label,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                    color = if (isSelected) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    },
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Button(
                    onClick = onPickFromGallery,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                ) {
                    Icon(
                        imageVector = Icons.Rounded.PermMedia,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "从相册选择",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
                if (currentAvatar.isNotBlank()) {
                    OutlinedButton(
                        onClick = onResetDefault,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.height(48.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.SettingsBackupRestore,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "恢复默认",
                            style = MaterialTheme.typography.labelLarge,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AccountMonogramBadge(
    userName: String,
    studentId: String,
) {
    val accent = ColorPool.hash(studentId.ifBlank { userName })
    val initial = userName
        .substringAfterLast('-')
        .trim()
        .firstOrNull()
        ?.uppercaseChar()
        ?.toString()
        ?: studentId.takeLast(2).ifBlank { "学" }

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = accent.copy(alpha = 0.16f),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.32f)),
        modifier = Modifier.size(46.dp),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = initial,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = CourseColorHelper.accentText(accent),
            )
        }
    }
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
                AccountMonogramBadge(
                    userName = user.userName,
                    studentId = user.studentId,
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
