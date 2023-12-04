package vip.mystery0.xhu.timetable.ui.activity

import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import vip.mystery0.xhu.timetable.R
import vip.mystery0.xhu.timetable.appName
import vip.mystery0.xhu.timetable.base.BaseComposeActivity
import vip.mystery0.xhu.timetable.config.store.UserStore
import vip.mystery0.xhu.timetable.ui.component.observerXhuDialogState
import vip.mystery0.xhu.timetable.ui.theme.MaterialIcons
import vip.mystery0.xhu.timetable.ui.theme.XhuColor
import vip.mystery0.xhu.timetable.utils.finishAllActivity
import vip.mystery0.xhu.timetable.viewmodel.LoginViewModel

class LoginActivity : BaseComposeActivity() {
    private val viewModel: LoginViewModel by viewModels()

    @OptIn(ExperimentalComposeUiApi::class)
    @Composable
    override fun BuildContent() {
        val keyboardController = LocalSoftwareKeyboardController.current
        val usernameFocusRequester = remember { FocusRequester() }
        val passwordFocusRequester = remember { FocusRequester() }
        var usernameError by remember { mutableStateOf(false) }
        var passwordError by remember { mutableStateOf(false) }

        Column(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Image(
                modifier = Modifier.fillMaxWidth(),
                painter = painterResource(id = R.mipmap.login_header),
                contentDescription = null,
                contentScale = ContentScale.FillWidth
            )
            Column(
                modifier = Modifier
                    .padding(horizontal = 48.dp)
                    .navigationBarsPadding()
                    .fillMaxHeight()
            ) {
                var username by remember { mutableStateOf("") }
                var password by remember { mutableStateOf("") }
                Spacer(
                    modifier = Modifier
                        .height(62.dp)
                        .fillMaxWidth()
                )
                OutlinedTextField(
                    modifier = Modifier
                        .height(IntrinsicSize.Min)
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
                        Icon(MaterialIcons.TwoTone.AccountCircle, null)
                    },
                    trailingIcon = {
                        if (username.isNotBlank()) {
                            IconButton(onClick = { username = "" }) {
                                Icon(MaterialIcons.TwoTone.Clear, null)
                            }
                        }
                    },
                    label = {
                        Text(text = "学号")
                    },
                    colors = TextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.secondary,
                        unfocusedTextColor = MaterialTheme.colorScheme.secondary,
                        unfocusedLabelColor = XhuColor.loginLabel,
//                        backgroundColor = Color.Transparent,
//                        leadingIconColor = MaterialTheme.colors.secondary,
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
                        .height(IntrinsicSize.Min)
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
                        Icon(MaterialIcons.TwoTone.Lock, null)
                    },
                    trailingIcon = {
                        if (password.isNotBlank()) {
                            IconButton(onClick = { password = "" }) {
                                Icon(MaterialIcons.TwoTone.Clear, null)
                            }
                        }
                    },
                    label = {
                        Text(text = "密码")
                    },
                    colors = TextFieldDefaults.colors(
//                        textColor = MaterialTheme.colors.secondary,
                        unfocusedLabelColor = XhuColor.loginLabel,
//                        backgroundColor = Color.Transparent,
//                        leadingIconColor = MaterialTheme.colors.secondary,
                    ),
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Done,
                        keyboardType = KeyboardType.Password,
                    ),
                    keyboardActions = KeyboardActions(onDone = {
                        if (doLogin(
                                username,
                                password,
                                usernameFocusRequester,
                                passwordFocusRequester
                            )
                        ) {
                            keyboardController?.hide()
                        }
                    }),
                    maxLines = 1,
                    isError = passwordError,
                )
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    text = "* 密码为教育系统密码（默认为18位身份证号）",
                    color = XhuColor.loginLabel,
                    fontSize = 12.sp,
                )
                Spacer(
                    modifier = Modifier
                        .height(60.dp)
                        .fillMaxWidth()
                )
                val loginState by viewModel.loginState.collectAsState()
                TextButton(
                    enabled = !loginState.loading,
                    modifier = Modifier
                        .fillMaxWidth(),
                    colors = ButtonDefaults.textButtonColors(
                        containerColor = MaterialTheme.colorScheme.secondary,
                        contentColor = Color.White,
                    ),
                    shape = RoundedCornerShape(36.dp),
                    onClick = {
                        if (doLogin(
                                username,
                                password,
                                usernameFocusRequester,
                                passwordFocusRequester
                            )
                        ) {
                            keyboardController?.hide()
                        }
                    }) {
                    Text(
                        text = "登 录",
                        fontSize = 16.sp
                    )
                }
            }
        }
        DialogContent()
    }

    private fun doLogin(
        username: String,
        password: String,
        usernameFocusRequester: FocusRequester,
        passwordFocusRequester: FocusRequester,
    ): Boolean {
        when {
            username.isBlank() -> {
                usernameFocusRequester.requestFocus()
                "用户名不能为空".toast()
                return false
            }

            password.isBlank() -> {
                passwordFocusRequester.requestFocus()
                "密码不能为空".toast()
                return false
            }

            else -> viewModel.login(username, password)
        }
        return true
    }

    @Composable
    private fun DialogContent() {
        val loginState by viewModel.loginState.collectAsState()
        ShowProgressDialog(
            showState = observerXhuDialogState(loginState.loading),
            text = "登录中..."
        )
        if (loginState.success) {
            "登录成功，欢迎使用${appName}！".toast()
            if (!intent.getBooleanExtra(AccountSettingsActivity.INTENT_EXTRA, false)) {
                intentTo(MainActivity::class)
            }
            finish()
        }
        if (loginState.errorMessage.isNotBlank()) {
            loginState.errorMessage.toast(true)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (!UserStore.blockIsLogin()) {
            finishAllActivity()
        }
    }
}