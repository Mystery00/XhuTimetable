package vip.mystery0.xhu.timetable.ui.activity

import androidx.activity.viewModels
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.koin.core.component.KoinComponent
import vip.mystery0.xhu.timetable.base.BaseComposeActivity
import vip.mystery0.xhu.timetable.config.GlobalConfig
import vip.mystery0.xhu.timetable.ui.preference.ConfigSettingsCheckbox
import vip.mystery0.xhu.timetable.ui.preference.XhuSettingsGroup
import vip.mystery0.xhu.timetable.ui.theme.ProfileImages
import vip.mystery0.xhu.timetable.ui.theme.XhuColor
import vip.mystery0.xhu.timetable.ui.theme.XhuIcons
import vip.mystery0.xhu.timetable.viewmodel.AccountManagementViewModel

class AccountSettingsActivity : BaseComposeActivity(), KoinComponent {
    companion object {
        const val INTENT_EXTRA = "LOGIN_FROM_SETTINGS"
    }

    private val viewModel: AccountManagementViewModel by viewModels()

    @Composable
    override fun BuildContent() {
        val errorMessage by viewModel.errorMessage.collectAsState()
        val editMode = remember { mutableStateOf(false) }
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
                    actions = {
                        IconButton(onClick = {
                            editMode.value = !editMode.value
                        }) {
                            Icon(
                                painter = if (editMode.value) XhuIcons.Action.done else XhuIcons.Action.manage,
                                contentDescription = null,
                                modifier = Modifier.size(24.dp),
                            )
                        }
                    }
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
                    Text(text = "多账号设置")
                }) {
                    ConfigSettingsCheckbox(
                        modifier = Modifier.padding(vertical = 8.dp),
                        config = GlobalConfig::multiAccountMode,
                        icon = {
                            Icon(
                                painter = XhuIcons.multiUser,
                                contentDescription = null,
                                tint = XhuColor.Common.blackText,
                            )
                        },
                        title = { Text(text = "启用情侣模式") },
                        subtitle = { Text(text = "注意：如果多个用户的课表存在冲突的情况，表格可能会变得很乱，请确定您开启这个模式的意义！") },
                        onCheckedChange = { newValue -> viewModel.changeMultiAccountMode(newValue) },
                    )
                }
                XhuSettingsGroup(title = {
                    Text(text = "账号管理")
                }) {
                    val loggedUserList by viewModel.loggedUserList.collectAsState()
                    loggedUserList.forEach { userItem ->
                        BuildItem(
                            painter = ProfileImages.hash(userItem.userName, userItem.sex == "男"),
                            text = "${userItem.studentId}(${userItem.userName})",
                            onClick = {
                                viewModel.changeMainUser(userItem.studentId)
                            },
                            onButtonClick = {
                                viewModel.logoutUser(userItem.studentId)
                            },
                            mainUser = userItem.main,
                            showButton = editMode.value,
                        )
                    }
                    if (!editMode.value) {
                        BuildItem(
                            painter = XhuIcons.add,
                            text = "登录其他账号",
                            onClick = {
                                intentTo(LoginActivity::class) {
                                    it.putExtra(INTENT_EXTRA, true)
                                }
                            },
                            onButtonClick = {},
                            mainUser = false,
                            showButton = false,
                        )
                    }
                }
            }
        }
        if (errorMessage.isNotBlank()) {
            errorMessage.toast(true)
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadLoggedUserList()
    }
}

@Composable
private fun BuildItem(
    painter: Painter,
    text: String,
    onClick: () -> Unit,
    onButtonClick: () -> Unit,
    mainUser: Boolean,
    showButton: Boolean = true,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        elevation = 0.dp,
        backgroundColor = XhuColor.accountCardBackground,
    ) {
        Box {
            if (mainUser) {
                Text(
                    text = "主用户",
                    fontSize = 8.sp,
                    color = Color(0xFF2196F3),
                    modifier = Modifier
                        .background(
                            color = Color(0xFFBBDEFB),
                            shape = RoundedCornerShape(bottomEnd = 8.dp),
                        )
                        .padding(1.dp),
                )
            }
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Image(
                    modifier = Modifier
                        .background(
                            color = XhuColor.Common.grayBackground,
                            shape = MaterialTheme.shapes.medium,
                        )
                        .size(48.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    painter = painter,
                    contentDescription = null
                )
                Text(
                    text = text,
                    fontSize = 14.sp,
                    modifier = Modifier.weight(1F),
                )
                if (showButton) {
                    TextButton(
                        onClick = onButtonClick,
                        colors = ButtonDefaults.textButtonColors(
                            backgroundColor = Color.Red,
                            contentColor = Color.White,
                        ),
                        modifier = Modifier.height(24.dp),
                        contentPadding = PaddingValues(0.dp),
                    ) {
                        Text(text = "退出登录", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}