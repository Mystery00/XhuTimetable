package vip.mystery0.xhu.timetable.ui.preference

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.alorma.compose.settings.storage.base.rememberBooleanSettingState
import com.alorma.compose.settings.ui.SettingsCheckbox
import com.alorma.compose.settings.ui.SettingsMenuLink
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.reflect.KMutableProperty0

@Composable
fun ConfigSettingsCheckbox(
    modifier: Modifier = Modifier,
    config: KMutableProperty0<Boolean>,
    scope: CoroutineScope = rememberCoroutineScope(),
    icon: @Composable (() -> Unit)? = null,
    title: @Composable () -> Unit,
    subtitle: @Composable (() -> Unit)? = null,
    onCheckedChange: suspend (Boolean) -> Unit = { newValue ->
        config.set(newValue)
    },
) {
    SettingsCheckbox(
        modifier = modifier,
        state = rememberBooleanSettingState(config.get()),
        icon = icon,
        title = title,
        subtitle = subtitle,
        onCheckedChange = { newValue -> scope.launch { onCheckedChange(newValue) } },
    )
}

typealias ConfigSetter<T> = (T) -> Unit

@Composable
fun <T> ConfigSettingsMenuLink(
    modifier: Modifier = Modifier,
    config: KMutableProperty0<T>,
    scope: CoroutineScope = rememberCoroutineScope(),
    icon: (@Composable (T) -> Unit)? = null,
    title: @Composable (T) -> Unit,
    subtitle: (@Composable (T) -> Unit)? = null,
    action: (@Composable (T, ConfigSetter<T>) -> Unit)? = null,
    onClick: suspend (T, ConfigSetter<T>) -> Unit = { _, _ -> },
) {
    var value by remember { mutableStateOf(config.get()) }
    val setter: ConfigSetter<T> = { newValue ->
        scope.launch { config.set(newValue) }
        value = newValue
    }
    SettingsMenuLink(
        modifier = modifier,
        icon = if (icon == null) null else {
            {
                icon(value)
            }
        },
        title = {
            title(value)
        },
        subtitle = if (subtitle == null) null else {
            {
                subtitle(value)
            }
        },
        action = if (action == null) null else {
            {
                action(value, setter)
            }
        },
        onClick = {
            scope.launch { onClick(value, setter) }
        }
    )
}