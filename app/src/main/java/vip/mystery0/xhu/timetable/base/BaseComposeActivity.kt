package vip.mystery0.xhu.timetable.base

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.DrawableRes
import androidx.annotation.RawRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.maxkeppeker.sheets.core.models.base.Header
import com.maxkeppeker.sheets.core.utils.BaseValues
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import org.koin.core.component.KoinComponent
import vip.mystery0.xhu.timetable.R
import vip.mystery0.xhu.timetable.ui.component.XhuDialogState
import vip.mystery0.xhu.timetable.ui.theme.XhuIcons
import vip.mystery0.xhu.timetable.ui.theme.XhuTimetableTheme
import kotlin.reflect.KClass

abstract class BaseComposeActivity : ComponentActivity(), KoinComponent {
    private var toast: Toast? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        initIntent()
        setContent {
            BuildContentWindow()
        }
    }

    open fun initIntent() {}

    @Composable
    open fun BuildContentWindow() {
        XhuTimetableTheme {
            BuildContent()
        }
    }

    @Composable
    open fun BuildContent() {
    }

    @Composable
    fun HandleErrorMessage(
        flow: MutableStateFlow<String>,
        showLong: Boolean = true,
    ) {
        val errorMessage by flow.collectAsState()
        HandleErrorMessage(errorMessage = errorMessage, showLong = showLong) {
            flow.value = ""
        }
    }

    @Composable
    fun HandleErrorMessage(
        errorMessage: String,
        showLong: Boolean = true,
        cancel: () -> Unit,
    ) {
        errorMessage.notBlankToast(showLong)
        LaunchedEffect(errorMessage) {
            if (errorMessage.isNotBlank()) {
                delay(5000)
                cancel()
            }
        }
    }

    fun <T : Activity> intentTo(
        clazz: KClass<T>,
        block: (Intent) -> Unit = {},
    ) {
        startActivity(Intent(this, clazz.java).apply(block))
    }

    fun String.toast(showLong: Boolean = false) =
        newToast(
            this@BaseComposeActivity,
            this,
            if (showLong) Toast.LENGTH_LONG else Toast.LENGTH_SHORT
        )

    fun toastString(message: String, showLong: Boolean = false) =
        newToast(
            this@BaseComposeActivity,
            message,
            if (showLong) Toast.LENGTH_LONG else Toast.LENGTH_SHORT
        )

    protected fun String.notBlankToast(showLong: Boolean = false) {
        if (this.isNotBlank()) {
            newToast(
                this@BaseComposeActivity,
                this,
                if (showLong) Toast.LENGTH_LONG else Toast.LENGTH_SHORT
            )
        }
    }

    private fun newToast(context: Context, message: String?, length: Int) {
        toast?.cancel()
        toast = Toast.makeText(context, message, length)
        toast?.show()
    }

    @Composable
    protected fun ShowProgressDialog(
        showState: XhuDialogState,
        text: String,
    ) {
        if (!showState.showing) {
            return
        }
        Dialog(
            onDismissRequest = { },
            properties = DialogProperties(
                dismissOnBackPress = false,
                dismissOnClickOutside = false,
            )
        ) {
            Surface(
                modifier = Modifier
                    .size(144.dp),
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 24.dp
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(72.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = text,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }

    @Composable
    fun BuildNoDataLayout() {
        BuildLayout(
            resId = R.raw.lottie_no_data,
            text = "",
            appendLayout = {
                Text(
                    text = "暂无数据",
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 48.dp),
                    fontSize = 20.sp
                )
            })
    }

    @Composable
    fun BuildNoPermissionLayout(
        permissionDescription: String,
        onRequestPermission: () -> Unit = {},
    ) {
        BuildLayout(
            resId = R.raw.lottie_no_permission,
            lottieHeight = 160.dp,
            lottieWidth = 325.dp,
            text = permissionDescription,
            appendLayout = {
                Button(onClick = onRequestPermission) {
                    Text("授予权限")
                }
            }
        )
    }

    @Composable
    private fun BuildLayout(
        @RawRes resId: Int,
        lottieHeight: Dp = 256.dp,
        lottieWidth: Dp = 256.dp,
        text: String,
        appendLayout: (@Composable () -> Unit)? = null,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .requiredHeightIn(min = 640.dp)
                .fillMaxHeight()
                .background(MaterialTheme.colorScheme.surface),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(resId))
                LottieAnimation(
                    composition = composition,
                    iterations = LottieConstants.IterateForever,
                    modifier = Modifier
                        .height(lottieHeight)
                        .width(lottieWidth)
                )
                if (text.isNotBlank()) {
                    Text(
                        text = text,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 48.dp)
                    )
                }
                appendLayout?.invoke()
            }
        }
    }

    @Composable
    fun BuildSearchText(
        searchText: String,
        placeholderText: String = "",
        onSearchTextChanged: (String) -> Unit = {},
        onClearClick: () -> Unit = {},
    ) {
        var showClearButton by remember { mutableStateOf(false) }
        val keyboardController = LocalSoftwareKeyboardController.current
        val focusRequester = remember { FocusRequester() }

        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 2.dp)
                .onFocusChanged { focusState ->
                    showClearButton = (focusState.isFocused)
                }
                .focusRequester(focusRequester),
            value = searchText,
            onValueChange = onSearchTextChanged,
            placeholder = {
                Text(text = placeholderText)
            },
            colors = TextFieldDefaults.colors(
                focusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                unfocusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                cursorColor = LocalContentColor.current.copy(alpha = LocalContentAlpha.current)
            ),
            trailingIcon = {
                AnimatedVisibility(
                    visible = showClearButton,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    IconButton(onClick = {
                        onClearClick()
                    }) {
                        Icon(
                            painter = XhuIcons.close,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface,
                        )
                    }

                }
            },
            maxLines = 1,
            singleLine = true,
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = {
                keyboardController?.hide()
            }),
        )

        LaunchedEffect(Unit) {
            focusRequester.requestFocus()
        }
    }

    @Composable
    fun BuildPageFooter(text: String) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(text = text, fontSize = 14.sp, color = MaterialTheme.colorScheme.outline)
        }
    }

    protected inline fun <reified T : BaseComposeActivity> pushDynamicShortcuts(
        @DrawableRes iconResId: Int,
        id: String = this.javaClass.name,
        label: String = title.toString(),
    ) {
        val shortcut = ShortcutInfoCompat.Builder(this, id)
            .setShortLabel(label)
            .setLongLabel(label)
            .setIcon(IconCompat.createWithResource(this, iconResId))
            .setIntent(Intent(this, T::class.java).apply {
                action = "${packageName}.${T::class.java.simpleName}"
            })
            .build()
        ShortcutManagerCompat.pushDynamicShortcut(this, shortcut)
    }

    @Composable
    protected fun LazyListState.isScrollingUp(): Boolean {
        var previousIndex by remember(this) { mutableIntStateOf(firstVisibleItemIndex) }
        var previousScrollOffset by remember(this) { mutableIntStateOf(firstVisibleItemScrollOffset) }
        return remember(this) {
            derivedStateOf {
                if (previousIndex != firstVisibleItemIndex) {
                    previousIndex > firstVisibleItemIndex
                } else {
                    previousScrollOffset >= firstVisibleItemScrollOffset
                }.also {
                    previousIndex = firstVisibleItemIndex
                    previousScrollOffset = firstVisibleItemScrollOffset
                }
            }
        }.value
    }

    fun xhuHeader(title: String): Header =
        Header.Custom {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(BaseValues.CONTENT_DEFAULT_PADDING)
                    .padding(top = 24.dp),
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Start
                )
            }
        }
}