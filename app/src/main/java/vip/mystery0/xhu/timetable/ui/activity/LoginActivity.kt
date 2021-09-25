package vip.mystery0.xhu.timetable.ui.activity

import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.twotone.AccountCircle
import androidx.compose.material.icons.twotone.Lock
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
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

class LoginActivity : BaseComposeActivity() {
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
                    var username by remember { mutableStateOf("000020210922") }
                    var password by remember { mutableStateOf("089fc9") }
                    Spacer(
                        modifier = Modifier
                            .height(62.dp)
                            .fillMaxWidth()
                    )
                    TextField(
                        modifier = Modifier.height(IntrinsicSize.Min),
                        value = username,
                        onValueChange = {
                            username = it
                        },
                        leadingIcon = {
                            Icon(Icons.TwoTone.AccountCircle, null)
                        },
                        label = {
                            Text(text = "学号")
                        },
                        colors = TextFieldDefaults.textFieldColors(
                            textColor = MaterialTheme.colors.primary,
                            unfocusedLabelColor = XhuColor.loginLabel,
                            backgroundColor = Color.Transparent,
                            disabledIndicatorColor = XhuColor.loginLabel,
                            leadingIconColor = MaterialTheme.colors.primary,
                        ),
                    )
                    Spacer(
                        modifier = Modifier
                            .height(48.dp)
                            .fillMaxWidth()
                    )
                    TextField(
                        modifier = Modifier
                            .height(IntrinsicSize.Min)
                            .fillMaxWidth(),
                        value = password,
                        onValueChange = {
                            password = it
                        },
                        leadingIcon = {
                            Icon(Icons.TwoTone.Lock, null)
                        },
                        label = {
                            Text(text = "密码")
                        },
                        colors = TextFieldDefaults.textFieldColors(
                            textColor = MaterialTheme.colors.primary,
                            unfocusedLabelColor = XhuColor.loginLabel,
                            backgroundColor = Color.Transparent,
                            disabledIndicatorColor = XhuColor.loginLabel,
                            leadingIconColor = MaterialTheme.colors.primary,
                        ),
                        visualTransformation = PasswordVisualTransformation()
                    )
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = "* 密码为教育系统密码（默认为18位身份证号）",
                        color = XhuColor.loginLabel,
                        fontSize = 12.sp,
                    )
                    Spacer(
                        modifier = Modifier
                            .height(62.dp)
                            .fillMaxWidth()
                    )
                    val loginState by viewModel.loginState.collectAsState()
                    TextButton(
                        enabled = !loginState.loading,
                        modifier = Modifier
                            .fillMaxWidth(),
                        colors = ButtonDefaults.textButtonColors(
                            backgroundColor = XhuColor.loginText,
                            contentColor = Color.White,
                        ),
                        shape = RoundedCornerShape(36.dp),
                        onClick = {
                            viewModel.login(username, password)
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
            "登录成功，欢迎使用${appName}！".toast(true)
            intentTo(MainActivity::class)
        }
        if (loginState.errorMessage.isNotBlank()) {
            loginState.errorMessage.toast(true)
        }
    }
}