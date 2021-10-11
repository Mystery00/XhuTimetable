package vip.mystery0.xhu.timetable.ui.preference

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.alorma.settings.composables.SettingsCheckbox
import com.alorma.settings.composables.SettingsMenuLink
import com.alorma.settings.storage.rememberBooleanSettingState
import kotlin.reflect.KMutableProperty0

@Composable
fun ConfigSettingsCheckbox(
    modifier: Modifier = Modifier,
    config: KMutableProperty0<Boolean>,
    icon: @Composable (() -> Unit)? = null,
    title: @Composable () -> Unit,
    subtitle: @Composable (() -> Unit)? = null,
    onCheckedChange: (Boolean) -> Unit = config::set,
) {
    SettingsCheckbox(
        modifier = modifier,
        state = rememberBooleanSettingState(config.get()),
        icon = icon,
        title = title,
        subtitle = subtitle,
        onCheckedChange = onCheckedChange,
    )
}

typealias ConfigSetter<T> = (T) -> Unit

@Composable
fun <T> ConfigSettingsMenuLink(
    modifier: Modifier = Modifier,
    config: KMutableProperty0<T>,
    icon: (@Composable (T) -> Unit)? = null,
    title: @Composable (T) -> Unit,
    subtitle: (@Composable (T) -> Unit)? = null,
    action: (@Composable (T, ConfigSetter<T>) -> Unit)? = null,
    onClick: (T, ConfigSetter<T>) -> Unit = { _, _ -> },
) {
    var value by remember { mutableStateOf(config.get()) }
    val setter: ConfigSetter<T> = {
        config.set(it)
        value = it
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
            onClick(value, setter)
        },
    )
}