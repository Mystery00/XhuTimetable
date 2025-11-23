package vip.mystery0.xhu.timetable.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.twotone.AccountCircle
import androidx.compose.material.icons.twotone.Clear
import androidx.compose.material.icons.twotone.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.maxkeppeker.sheets.core.models.base.rememberUseCaseState
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel
import vip.mystery0.xhu.timetable.base.appName
import vip.mystery0.xhu.timetable.config.toast.showLongToast
import vip.mystery0.xhu.timetable.config.toast.showShortToast
import vip.mystery0.xhu.timetable.module.PRIVACY_URL
import vip.mystery0.xhu.timetable.ui.component.ShowProgressDialog
import vip.mystery0.xhu.timetable.ui.navigation.LocalNavController
import vip.mystery0.xhu.timetable.ui.navigation.RouteLogin
import vip.mystery0.xhu.timetable.ui.navigation.RouteMain
import vip.mystery0.xhu.timetable.ui.navigation.replaceTo
import vip.mystery0.xhu.timetable.viewmodel.LoginViewModel
import xhutimetable.composeapp.generated.resources.Res
import xhutimetable.composeapp.generated.resources.login_header

@Composable
fun LoginScreen(fromAccountManager: Boolean) {
    val viewModel = koinViewModel<LoginViewModel>()

    val navController = LocalNavController.current!!
    val keyboardController = LocalSoftwareKeyboardController.current

    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val usernameFocusRequester = remember { FocusRequester() }
    val passwordFocusRequester = remember { FocusRequester() }
    var usernameError by remember { mutableStateOf(false) }
    var passwordError by remember { mutableStateOf(false) }
    var checkedPrivacy by remember { mutableStateOf(fromAccountManager) }

    val xhuDialogState = rememberUseCaseState()

    val loginState by viewModel.loginState.collectAsState()

    fun doLogin(): Boolean {
        when {
            username.isBlank() -> {
                usernameFocusRequester.requestFocus()
                showShortToast("用户名不能为空")
                return false
            }

            password.isBlank() -> {
                passwordFocusRequester.requestFocus()
                showShortToast("密码不能为空")
                return false
            }

            !checkedPrivacy -> {
                showLongToast("请阅读并同意隐私协议")
                return false
            }

            else -> viewModel.login(username, password)
        }
        return true
    }

    LaunchedEffect(Unit) {
        viewModel.init()
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Image(
            modifier = Modifier.fillMaxWidth(),
            painter = painterResource(Res.drawable.login_header),
            contentDescription = null,
            contentScale = ContentScale.FillWidth
        )
        Column(
            modifier = Modifier
                .padding(horizontal = 48.dp)
                .fillMaxHeight()
        ) {
            Spacer(
                modifier = Modifier
                    .height(62.dp)
                    .fillMaxWidth()
            )
            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { focusState ->
                        if (focusState.isFocused) {
                            //持有焦点
                            usernameError = username.isBlank()
                        }
                    }
                    .focusRequester(usernameFocusRequester),
                value = username,
                onValueChange = {
                    username = it
                },
                shape = RoundedCornerShape(18.dp),
                leadingIcon = {
                    Icon(Icons.TwoTone.AccountCircle, null)
                },
                trailingIcon = {
                    if (username.isNotBlank()) {
                        IconButton(onClick = { username = "" }) {
                            Icon(Icons.TwoTone.Clear, null)
                        }
                    }
                },
                label = {
                    Text(text = "学号")
                },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedTextColor = MaterialTheme.colorScheme.primary,
                    unfocusedTextColor = MaterialTheme.colorScheme.primary,
                ),
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Next,
                    keyboardType = KeyboardType.Number,
                ),
                maxLines = 1,
                isError = usernameError,
            )
            Spacer(
                modifier = Modifier
                    .height(48.dp)
                    .fillMaxWidth()
            )
            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { focusState ->
                        if (focusState.isFocused) {
                            //持有焦点
                            passwordError = password.isBlank()
                        }
                    }
                    .focusRequester(passwordFocusRequester),
                value = password,
                onValueChange = {
                    password = it
                },
                shape = RoundedCornerShape(18.dp),
                leadingIcon = {
                    Icon(Icons.TwoTone.Lock, null)
                },
                trailingIcon = {
                    if (password.isNotBlank()) {
                        IconButton(onClick = { password = "" }) {
                            Icon(Icons.TwoTone.Clear, null)
                        }
                    }
                },
                label = {
                    Text(text = "密码")
                },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedTextColor = MaterialTheme.colorScheme.primary,
                    unfocusedTextColor = MaterialTheme.colorScheme.primary,
                ),
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Done,
                    keyboardType = KeyboardType.Password,
                ),
                keyboardActions = KeyboardActions(onDone = {
                    if (doLogin()
                    ) {
                        keyboardController?.hide()
                    }
                }),
                maxLines = 1,
                isError = passwordError,
            )
            val loginLabel by viewModel.loginLabel.collectAsState()
            if (loginLabel.isNotBlank()) {
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    text = loginLabel,
                    color = MaterialTheme.colorScheme.outline,
                    fontSize = 12.sp,
                )
            }
            Spacer(
                modifier = Modifier
                    .height(30.dp)
                    .fillMaxWidth()
            )
            if (!fromAccountManager) {
                Row(
                    modifier = Modifier.fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Checkbox(
                        checked = checkedPrivacy,
                        onCheckedChange = {
                            checkedPrivacy = it
                        },
                    )
                    val text = buildAnnotatedString {
                        withStyle(SpanStyle(color = MaterialTheme.colorScheme.outline)) {
                            append("我已阅读并同意")
                        }
                        withLink(LinkAnnotation.Url(PRIVACY_URL)) {
                            append("《隐私协议》")
                        }
                    }
                    Text(
                        text = text,
                        fontSize = 14.sp,
                    )
                }
            }
            Spacer(
                modifier = Modifier
                    .height(30.dp)
                    .fillMaxWidth()
            )
            Button(
                enabled = !loginState.loading,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ),
                shape = RoundedCornerShape(36.dp),
                onClick = {
                    if (doLogin()
                    ) {
                        keyboardController?.hide()
                    }
                }) {
                Text(
                    text = "登 录",
                    fontSize = 16.sp
                )
            }
            if (fromAccountManager) {
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary,
                        contentColor = MaterialTheme.colorScheme.onSecondary,
                    ),
                    shape = RoundedCornerShape(36.dp),
                    onClick = {
                        navController.popBackStack()
                    }) {
                    Text(
                        text = "返回",
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
    ShowProgressDialog(
        useCaseState = xhuDialogState,
        text = "登录中...",
        successText = if (loginState.success) "登录成功" else "",
        errorText = loginState.errorMessage,
    )
    LaunchedEffect(loginState) {
        if (loginState.loading) {
            xhuDialogState.show()
        } else if (!loginState.success) {
            delay(1500L)
            xhuDialogState.hide()
        }
        if (loginState.success) {
            showShortToast("登录成功，欢迎使用${appName()}！")
            delay(500L)
            if (fromAccountManager) {
                navController.popBackStack()
            } else {
                navController.replaceTo<RouteLogin>(RouteMain)
            }
        }
    }
}
