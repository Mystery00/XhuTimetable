package vip.mystery0.xhu.timetable.ui.activity

import androidx.activity.viewModels
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alorma.settings.composables.SettingsCheckbox
import com.alorma.settings.storage.rememberBooleanSettingState
import org.koin.core.component.KoinComponent
import vip.mystery0.xhu.timetable.base.BaseComposeActivity
import vip.mystery0.xhu.timetable.config.Config
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
        val loading by viewModel.loading.collectAsState()
        val loggedUserList by viewModel.loggedUserList.collectAsState()
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
                            Icon(XhuIcons.back, "")
                        }
                    },
                    actions = {
                        Text(
                            text = if (editMode.value) "取消" else "管理",
                            modifier = Modifier.clickable(
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() },
                            ) {
                                editMode.value = !editMode.value
                            })
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
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                SettingsCheckbox(
                    modifier = Modifier.padding(vertical = 8.dp),
                    state = rememberBooleanSettingState(Config.multiAccountMode),
                    icon = { Icon(painter = XhuIcons.multiAccount, contentDescription = null) },
                    title = { Text(text = "启用多用户模式") },
                    subtitle = { Text(text = "注意：如果多个用户的课表存在冲突的情况，表格可能会变得很乱，请确定您开启这个模式的意义！") },
                    onCheckedChange = { newValue -> viewModel.changeMultiAccountMode(newValue) },
                )
                Text(
                    text = if (editMode.value) "清除账号登陆记录" else "轻触头像以切换账号",
                    modifier = Modifier
                        .padding(vertical = 36.dp),
                    fontSize = 18.sp,
                )
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
                        text = "登陆其他账号",
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
                        .size(48.dp),
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