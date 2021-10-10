package vip.mystery0.xhu.timetable.ui.activity

import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.twotone.AccountCircle
import androidx.compose.material.icons.twotone.Clear
import androidx.compose.material.icons.twotone.Lock
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.google.accompanist.insets.ProvideWindowInsets
import com.google.accompanist.insets.navigationBarsPadding
import vip.mystery0.xhu.timetable.R
import vip.mystery0.xhu.timetable.appName
import vip.mystery0.xhu.timetable.base.BaseComposeActivity
import vip.mystery0.xhu.timetable.config.DataHolder
import vip.mystery0.xhu.timetable.model.response.Version
import vip.mystery0.xhu.timetable.ui.theme.XhuColor
import vip.mystery0.xhu.timetable.viewmodel.LoginViewModel

class LoginActivity : BaseComposeActivity(setSystemUiColor = false) {
    private val viewModel: LoginViewModel by viewModels()

    private val version: Version
        get() = DataHolder.version!!

    @Composable
    override fun BuildContent() {
        ProvideWindowInsets {
            Column(modifier = Modifier.fillMaxWidth()) {
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
                            .fillMaxWidth(),
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
                        isError = username.isBlank(),
                    )
                    Spacer(
                        modifier = Modifier
                            .height(48.dp)
                            .fillMaxWidth()
                    )
                    OutlinedTextField(
                        modifier = Modifier
                            .height(IntrinsicSize.Min)
                            .fillMaxWidth(),
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
                        maxLines = 1,
                        isError = password.isBlank(),
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
                            when {
                                username.isBlank() -> "用户名不能为空".toast()
                                password.isBlank() -> "密码不能为空".toast()
                                else -> viewModel.login(username, password)
                            }
                        }) {
                        Text(
                            text = "登 录",
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }
        DialogContent()
    }

    @Composable
    private fun DialogContent() {
        val loginState by viewModel.loginState.collectAsState()
        if (loginState.loading) {
            Dialog(
                onDismissRequest = {
                    "取消登录操作".toast()
                },
                DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
            ) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .background(Color.White, shape = RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .padding(6.dp, 0.dp, 0.dp, 0.dp)
                        )
                        Text(
                            text = "登录中……",
                            modifier = Modifier
                                .padding(0.dp, 8.dp, 0.dp, 0.dp)
                        )
                    }
                }
            }
        }
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
}