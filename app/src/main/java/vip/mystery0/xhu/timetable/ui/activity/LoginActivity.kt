package vip.mystery0.xhu.timetable.ui.activity

import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.*
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
import com.zyao89.view.zloading.Z_TYPE
import vip.mystery0.xhu.timetable.R
import vip.mystery0.xhu.timetable.appName
import vip.mystery0.xhu.timetable.base.BaseComposeActivity
import vip.mystery0.xhu.timetable.config.SessionManager
import vip.mystery0.xhu.timetable.ui.theme.MaterialIcons
import vip.mystery0.xhu.timetable.ui.theme.XhuColor
import vip.mystery0.xhu.timetable.utils.finishAllActivity
import vip.mystery0.xhu.timetable.viewmodel.LoginViewModel

class LoginActivity : BaseComposeActivity(setSystemUiColor = false) {
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
                .background(XhuColor.Common.whiteBackground)
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
                    colors = TextFieldDefaults.textFieldColors(
                        textColor = MaterialTheme.colors.secondary,
                        unfocusedLabelColor = XhuColor.loginLabel,
                        backgroundColor = Color.Transparent,
                        leadingIconColor = MaterialTheme.colors.secondary,
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
                    colors = TextFieldDefaults.textFieldColors(
                        textColor = MaterialTheme.colors.secondary,
                        unfocusedLabelColor = XhuColor.loginLabel,
                        backgroundColor = Color.Transparent,
                        leadingIconColor = MaterialTheme.colors.secondary,
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
                        backgroundColor = MaterialTheme.colors.secondary,
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
        ShowProgressDialog(show = loginState.loading, text = "登录中……", type = Z_TYPE.STAR_LOADING)
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
//        if (!SessionManager.isLogin()) {
//            finishAllActivity()
//        }
    }
}