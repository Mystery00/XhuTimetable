package vip.mystery0.xhu.timetable.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.HelpOutline
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel
import vip.mystery0.xhu.timetable.base.appName
import vip.mystery0.xhu.timetable.config.toast.showLongToast
import vip.mystery0.xhu.timetable.config.toast.showShortToast
import vip.mystery0.xhu.timetable.module.HELP_URL
import vip.mystery0.xhu.timetable.module.PRIVACY_URL
import vip.mystery0.xhu.timetable.ui.component.dialog.XhuDialog
import vip.mystery0.xhu.timetable.ui.component.dialog.rememberXhuDialogState
import vip.mystery0.xhu.timetable.ui.navigation.LocalNavController
import vip.mystery0.xhu.timetable.ui.navigation.RouteLogin
import vip.mystery0.xhu.timetable.ui.navigation.RouteMain
import vip.mystery0.xhu.timetable.ui.navigation.replaceTo
import vip.mystery0.xhu.timetable.ui.theme.XhuIcons
import vip.mystery0.xhu.timetable.viewmodel.LoginViewModel
import xhutimetable.composeapp.generated.resources.Res
import xhutimetable.composeapp.generated.resources.ic_app_icon
import xhutimetable.composeapp.generated.resources.login_header

@Composable
fun LoginScreen(fromAccountManager: Boolean) {
    val viewModel = koinViewModel<LoginViewModel>()
    val navController = LocalNavController.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val uriHandler = LocalUriHandler.current

    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    val usernameFocusRequester = remember { FocusRequester() }
    val passwordFocusRequester = remember { FocusRequester() }
    var usernameError by remember { mutableStateOf(false) }
    var passwordError by remember { mutableStateOf(false) }
    var checkedPrivacy by remember { mutableStateOf(true) }

    val helpDialogState = rememberXhuDialogState()
    val loginState by viewModel.loginState.collectAsState()
    val loginLabel by viewModel.loginLabel.collectAsState()

    fun doLogin(): Boolean {
        usernameError = username.isBlank()
        passwordError = password.isBlank()
        when {
            usernameError -> {
                usernameFocusRequester.requestFocus()
                showShortToast("请输入学号")
                return false
            }

            passwordError -> {
                passwordFocusRequester.requestFocus()
                showShortToast("请输入密码")
                return false
            }

            !checkedPrivacy -> {
                showLongToast("请阅读并同意隐私协议")
                return false
            }

            else -> viewModel.login(username.trim(), password)
        }
        return true
    }

    LaunchedEffect(Unit) {
        viewModel.init()
    }

    LaunchedEffect(loginState.showHelpDialog) {
        if (loginState.showHelpDialog) {
            helpDialogState.show()
        }
    }

    LaunchedEffect(loginState.success) {
        if (loginState.success) {
            showShortToast("登录成功，欢迎使用${appName()}！")
            if (fromAccountManager) {
                navController.popBackStack()
            } else {
                navController.replaceTo<RouteLogin>(RouteMain)
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding()
                .imePadding()
                .verticalScroll(rememberScrollState()),
        ) {
            // 顶部校园插画头图 + 柔和渐变衔接
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(236.dp),
            ) {
                Image(
                    painter = painterResource(Res.drawable.login_header),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Black.copy(alpha = 0.18f),
                                    Color.Transparent,
                                    MaterialTheme.colorScheme.background.copy(alpha = 0.72f),
                                    MaterialTheme.colorScheme.background,
                                )
                            )
                        )
                )

                // 从账号管理页跳转时显示浮层返回按钮
                if (fromAccountManager) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Surface(
                            onClick = { navController.popBackStack() },
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.82f),
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                Icon(
                                    painter = XhuIcons.back,
                                    contentDescription = "返回",
                                    modifier = Modifier.size(18.dp),
                                )
                                Text(
                                    text = "返回",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Medium,
                                )
                            }
                        }
                    }
                }

                // 品牌 Logo 徽章骑缝居中
                Surface(
                    shape = RoundedCornerShape(22.dp),
                    color = Color(0xFF3DDC84),
                    shadowElevation = 8.dp,
                    border = BorderStroke(2.dp, MaterialTheme.colorScheme.background),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .size(74.dp),
                ) {
                    Image(
                        painter = painterResource(Res.drawable.ic_app_icon),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(22.dp)),
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
            ) {
                Spacer(modifier = Modifier.height(12.dp))

                // 品牌标题区
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        text = if (fromAccountManager) "登录新账号" else "欢迎使用西瓜课表",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                    val subtitleLinkColor = MaterialTheme.colorScheme.primary
                    val subtitleText = buildAnnotatedString {
                        withStyle(SpanStyle(color = MaterialTheme.colorScheme.onSurfaceVariant)) {
                            append("请使用西华大学")
                        }
                        withLink(
                            LinkAnnotation.Url(
                                url = "https://jwc.xhu.edu.cn",
                                styles = TextLinkStyles(
                                    style = SpanStyle(
                                        color = subtitleLinkColor,
                                        fontWeight = FontWeight.Medium,
                                        textDecoration = TextDecoration.Underline,
                                    )
                                )
                            )
                        ) {
                            append("教务系统")
                        }
                        withStyle(SpanStyle(color = MaterialTheme.colorScheme.onSurfaceVariant)) {
                            append("账号登录")
                        }
                    }
                    Text(
                        text = subtitleText,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }

            Spacer(modifier = Modifier.height(32.dp))

            // 登录表单卡片
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                border = BorderStroke(
                    0.5.dp,
                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)
                ),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    OutlinedTextField(
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(usernameFocusRequester),
                        value = username,
                        onValueChange = {
                            username = it
                            if (usernameError && it.isNotBlank()) usernameError = false
                        },
                        enabled = !loginState.loading,
                        shape = RoundedCornerShape(14.dp),
                        leadingIcon = {
                            Icon(Icons.Rounded.AccountCircle, contentDescription = null)
                        },
                        trailingIcon = {
                            if (username.isNotBlank()) {
                                IconButton(onClick = { username = "" }) {
                                    Icon(Icons.Rounded.Clear, contentDescription = "清除学号")
                                }
                            }
                        },
                        label = { Text(text = "学号") },
                        supportingText = if (usernameError) {
                            { Text("请输入学号") }
                        } else null,
                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Next,
                            keyboardType = KeyboardType.Number,
                        ),
                        singleLine = true,
                        isError = usernameError,
                    )

                    OutlinedTextField(
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(passwordFocusRequester),
                        value = password,
                        onValueChange = {
                            password = it
                            if (passwordError && it.isNotBlank()) passwordError = false
                        },
                        enabled = !loginState.loading,
                        shape = RoundedCornerShape(14.dp),
                        leadingIcon = {
                            Icon(Icons.Rounded.Lock, contentDescription = null)
                        },
                        trailingIcon = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (password.isNotBlank()) {
                                    IconButton(onClick = { password = "" }) {
                                        Icon(Icons.Rounded.Clear, contentDescription = "清除密码")
                                    }
                                }
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        imageVector = if (passwordVisible) {
                                            Icons.Rounded.VisibilityOff
                                        } else {
                                            Icons.Rounded.Visibility
                                        },
                                        contentDescription = if (passwordVisible) "隐藏密码" else "显示密码",
                                    )
                                }
                            }
                        },
                        label = { Text(text = "密码") },
                        supportingText = when {
                            passwordError -> ({ Text("请输入密码") })
                            loginLabel.isNotBlank() -> ({ Text(loginLabel) })
                            else -> null
                        },
                        visualTransformation = if (passwordVisible) {
                            VisualTransformation.None
                        } else {
                            PasswordVisualTransformation()
                        },
                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Done,
                            keyboardType = KeyboardType.Password,
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                if (doLogin()) {
                                    keyboardController?.hide()
                                }
                            }
                        ),
                        singleLine = true,
                        isError = passwordError,
                    )

                    // 错误提示卡片（内联展示，不阻塞用户修改密码）
                    val displayError = loginState.lastErrorMessage.ifBlank { loginState.errorMessage }
                    AnimatedVisibility(visible = displayError.isNotBlank() && !loginState.loading) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.7f),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.ErrorOutline,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onErrorContainer,
                                    modifier = Modifier.size(18.dp),
                                )
                                Text(
                                    text = displayError,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    modifier = Modifier.weight(1f),
                                )
                            }
                        }
                    }

                    // 隐私协议勾选
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .clickable(enabled = !loginState.loading) {
                                checkedPrivacy = !checkedPrivacy
                            }
                            .padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Checkbox(
                            checked = checkedPrivacy,
                            enabled = !loginState.loading,
                            onCheckedChange = { checkedPrivacy = it },
                        )
                        val linkColor = MaterialTheme.colorScheme.primary
                        val text = buildAnnotatedString {
                            withStyle(SpanStyle(color = MaterialTheme.colorScheme.onSurfaceVariant)) {
                                append("我已阅读并同意 ")
                            }
                            withLink(
                                LinkAnnotation.Url(
                                    url = PRIVACY_URL,
                                    styles = TextLinkStyles(
                                        style = SpanStyle(
                                            color = linkColor,
                                            fontWeight = FontWeight.Medium,
                                            textDecoration = TextDecoration.Underline,
                                        )
                                    )
                                )
                            ) {
                                append("《隐私协议》")
                            }
                        }
                        Text(
                            text = text,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }

                    // 主登录按钮（内置平滑 Loading 指示器）
                    Button(
                        enabled = !loginState.loading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(16.dp),
                        onClick = {
                            if (doLogin()) {
                                keyboardController?.hide()
                            }
                        }
                    ) {
                        if (loginState.loading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.2.dp,
                                color = MaterialTheme.colorScheme.onPrimary,
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "正在登录…",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                            )
                        } else {
                            Text(
                                text = "登 录",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextButton(onClick = { uriHandler.openUri(HELP_URL) }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.HelpOutline,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "登录遇到问题？获取帮助与反馈")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    XhuDialog(
        state = helpDialogState,
        title = "登录遇到困难？",
        icon = { Icon(Icons.AutoMirrored.Rounded.HelpOutline, contentDescription = null) },
        onDismiss = {
            helpDialogState.hide()
            viewModel.dismissHelpDialog()
        },
        dismissButtonText = "继续尝试",
        onDismissButtonClick = {
            helpDialogState.hide()
            viewModel.dismissHelpDialog()
        },
        confirmButtonText = "获取帮助",
        onConfirmButtonClick = {
            helpDialogState.hide()
            viewModel.dismissHelpDialog()
            uriHandler.openUri(HELP_URL)
        },
    ) {
        Text(
            text = "已连续尝试登录 3 次，仍未成功。你可以前往帮助与反馈页面，通过交流群、邮件或留言联系我们协助排查。",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
