package vip.mystery0.xhu.timetable.ui.activity

import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.koin.core.component.KoinComponent
import vip.mystery0.xhu.timetable.base.BaseComposeActivity
import vip.mystery0.xhu.timetable.config.store.ConfigStore
import vip.mystery0.xhu.timetable.model.Gender
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

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun BuildContent() {
        val errorMessage by viewModel.errorMessage.collectAsState()
        val editMode = remember { mutableStateOf(false) }
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(text = title.toString()) },
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
                    .verticalScroll(rememberScrollState()),
            ) {
                XhuSettingsGroup(title = {
                    Text(text = "多账号设置")
                }) {
                    ConfigSettingsCheckbox(
                        modifier = Modifier.padding(vertical = 8.dp),
                        config = ConfigStore::multiAccountMode,
                        icon = {
                            Icon(
                                painter = XhuIcons.multiUser,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurface,
                            )
                        },
                        title = { Text(text = "启用情侣模式") },
                        subtitle = { Text(text = "注意：如果多个用户的课表存在冲突的情况，表格可能会变得很乱，请确定您开启这个模式的意义！") },
                    ) { newValue -> viewModel.changeMultiAccountMode(newValue) }
                }
                XhuSettingsGroup(title = {
                    Text(text = "账号管理")
                }) {
                    val loggedUserList by viewModel.loggedUserList.collectAsState()
                    loggedUserList.forEach { userItem ->
                        BuildItem(
                            painter = ProfileImages.hash(
                                userItem.userName,
                                userItem.gender == Gender.MALE
                            ),
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
        colors = CardDefaults.cardColors(
            containerColor = XhuColor.cardBackground,
        ),
    ) {
        Box {
            if (mainUser) {
                Text(
                    text = "主用户",
                    fontSize = 8.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier
                        .background(
                            color = MaterialTheme.colorScheme.primaryContainer,
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
                            containerColor = Color.Red,
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