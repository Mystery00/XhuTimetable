package vip.mystery0.xhu.timetable.ui.activity

import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.maxkeppeker.sheets.core.models.base.Header
import com.maxkeppeker.sheets.core.models.base.SelectionButton
import com.maxkeppeker.sheets.core.models.base.rememberUseCaseState
import com.maxkeppeler.sheets.input.InputDialog
import com.maxkeppeler.sheets.input.models.InputCustomView
import com.maxkeppeler.sheets.input.models.InputSelection
import org.koin.core.component.KoinComponent
import vip.mystery0.xhu.timetable.base.BaseComposeActivity
import vip.mystery0.xhu.timetable.config.store.ConfigStore
import vip.mystery0.xhu.timetable.config.store.EventBus
import vip.mystery0.xhu.timetable.model.AccountTitleTemplate
import vip.mystery0.xhu.timetable.model.CustomAccountTitle
import vip.mystery0.xhu.timetable.model.Gender
import vip.mystery0.xhu.timetable.model.event.EventType
import vip.mystery0.xhu.timetable.ui.component.XhuDialogState
import vip.mystery0.xhu.timetable.ui.component.rememberXhuDialogState
import vip.mystery0.xhu.timetable.ui.preference.ConfigSettingsCheckbox
import vip.mystery0.xhu.timetable.ui.preference.XhuSettingsGroup
import vip.mystery0.xhu.timetable.ui.preference.XhuSettingsMenuLink
import vip.mystery0.xhu.timetable.ui.theme.ProfileImages
import vip.mystery0.xhu.timetable.ui.theme.XhuColor
import vip.mystery0.xhu.timetable.ui.theme.XhuIcons
import vip.mystery0.xhu.timetable.viewmodel.AccountManagementViewModel
import vip.mystery0.xhu.timetable.viewmodel.UserItem

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
            floatingActionButton = {
                AnimatedVisibility(
                    visible = !editMode.value,
                    enter = scaleIn(),
                    exit = scaleOut(),
                ) {
                    ExtendedFloatingActionButton(
                        text = { Text(text = "登录其他账号") },
                        onClick = {
                            intentTo(LoginActivity::class) {
                                it.putExtra(INTENT_EXTRA, true)
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = Icons.Rounded.Add,
                                contentDescription = "登录其他账号",
                            )
                        },
                    )
                }
            }
        ) { paddingValues ->
            val customTodayUserTemplateDialog = rememberXhuDialogState()
            val customWeekUserTemplateDialog = rememberXhuDialogState()

            val customAccountTitle by viewModel.customAccountTitle.collectAsState()

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
                        title = { Text(text = "多账号模式") },
                        subtitle = { Text(text = "将当前所有已登录账号的课表全部显示出来") },
                    ) {
                        EventBus.post(EventType.MULTI_MODE_CHANGED)
                    }
                    XhuSettingsMenuLink(
                        title = { Text(text = "今日课程界面账号信息模板") },
                        subtitle = {
                            Text(text = "启动多账号模式之后，使用该模板来显示对应的账号信息")
                        },
                        onClick = {
                            customTodayUserTemplateDialog.show()
                        }
                    )
                    XhuSettingsMenuLink(
                        title = { Text(text = "本周课程界面账号信息模板") },
                        subtitle = {
                            Text(text = "启动多账号模式之后，使用该模板来显示对应的账号信息")
                        },
                        onClick = {
                            customWeekUserTemplateDialog.show()
                        }
                    )
                }
                XhuSettingsGroup(title = {
                    Text(text = "账号管理")
                }) {
                    XhuSettingsMenuLink(
                        title = { Text(text = "长按用户卡片可强制更新用户信息") },
                        subtitle = { Text(text = "转了专业或者因为某些原因，导致教务系统的个人信息变更，可通过此方式更新服务端的缓存") }
                    )
                    val loggedUserList by viewModel.loggedUserList.collectAsState()
                    loggedUserList.forEach { userItem ->
                        BuildItem(
                            painter = ProfileImages.hash(
                                userItem.userName,
                                userItem.gender == Gender.MALE
                            ),
                            user = userItem,
                            onClick = {
                                viewModel.changeMainUser(userItem.studentId)
                            },
                            onLongClick = {
                                viewModel.reloadUserInfo(userItem.studentId)
                                "用户信息已更新".toast()
                            },
                            onButtonClick = {
                                viewModel.logoutUser(userItem.studentId)
                            },
                            mainUser = userItem.main,
                            showButton = editMode.value,
                        )
                    }
                }
                BuildCustomUserTemplateDialog(
                    value = customAccountTitle.todayTemplate,
                    resetValue = CustomAccountTitle.DEFAULT.todayTemplate,
                    show = customTodayUserTemplateDialog,
                    listener = { newValue ->
                        val copy = customAccountTitle.copy(todayTemplate = newValue)
                        viewModel.updateAccountTitleTemplate(copy)
                    })
                BuildCustomUserTemplateDialog(
                    value = customAccountTitle.weekTemplate,
                    resetValue = CustomAccountTitle.DEFAULT.weekTemplate,
                    show = customWeekUserTemplateDialog,
                    listener = { newValue ->
                        val copy = customAccountTitle.copy(weekTemplate = newValue)
                        viewModel.updateAccountTitleTemplate(copy)
                    })
            }
        }
        if (errorMessage.isNotBlank()) {
            errorMessage.toast(true)
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun BuildCustomUserTemplateDialog(
        value: String,
        resetValue: String,
        show: XhuDialogState,
        listener: (String) -> Unit,
    ) {
        val valueState = remember { mutableStateOf(value) }
        if (!show.showing) {
            return
        }
        val inputOptions = listOf(
            InputCustomView(view = {
                Column(modifier = Modifier.padding(4.dp)) {
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = valueState.value,
                        onValueChange = {
                            valueState.value = it
                        })
                    Spacer(modifier = Modifier.height(4.dp))
                    Row {
                        Text(text = "学号",
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .padding(4.dp)
                                .clickable {
                                    valueState.value += "{${AccountTitleTemplate.STUDENT_NO.tpl}}"
                                })
                        Text(text = "姓名",
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .padding(4.dp)
                                .clickable {
                                    valueState.value += "{${AccountTitleTemplate.NAME.tpl}}"
                                })
                        Text(text = "昵称",
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .padding(4.dp)
                                .clickable {
                                    valueState.value += "{${AccountTitleTemplate.NICK_NAME.tpl}}"
                                })
                    }
                }
            }),
        )
        InputDialog(
            header = Header.Default(title = "请输入模板内容"),
            state = rememberUseCaseState(
                visible = true,
                onCloseRequest = { show.hide() },
            ),
            selection = InputSelection(
                input = inputOptions,
                onPositiveClick = {
                    listener(valueState.value)
                },
                extraButton = SelectionButton(text = "重置"),
                onExtraButtonClick = {
                    valueState.value = resetValue
                    listener(resetValue)
                    show.hide()
                },
            )
        )
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadLoggedUserList()
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun BuildItem(
    painter: Painter,
    user: UserItem,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onButtonClick: () -> Unit,
    mainUser: Boolean,
    showButton: Boolean = true,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .combinedClickable(onClick = onClick, onLongClick = onLongClick),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = XhuColor.cardBackground,
        ),
    ) {
        Box {
            if (mainUser) {
                Text(
                    text = "主用户",
                    fontSize = 10.sp,
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
                        .clip(RoundedCornerShape(16.dp)),
                    painter = painter,
                    contentDescription = null
                )
                Column(modifier = Modifier.weight(1F)) {
                    Text(
                        text = "${user.studentId}(${user.userName})",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        buildString {
                            appendLine("年级：${user.xhuGrade}")
                            if (user.majorName.isNotBlank()) appendLine("专业：${user.majorName}")
                            if (user.college.isNotBlank()) appendLine("学院：${user.college}")
                            if (user.majorDirection.isNotBlank()) appendLine("专业方向：${user.majorDirection}")
                        },
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.outline,
                    )
                }
                AnimatedVisibility(
                    visible = showButton,
                    enter = fadeIn(),
                    exit = fadeOut(),
                ) {
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