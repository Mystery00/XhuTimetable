package vip.mystery0.xhu.timetable.ui.preference

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.alorma.compose.settings.storage.base.rememberBooleanSettingState
import com.alorma.compose.settings.ui.SettingsCheckbox
import com.alorma.compose.settings.ui.SettingsMenuLink
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import vip.mystery0.xhu.timetable.config.store.CacheStore
import vip.mystery0.xhu.timetable.config.store.ConfigStore
import vip.mystery0.xhu.timetable.config.store.GlobalCacheStore
import vip.mystery0.xhu.timetable.config.store.GlobalConfigStore
import vip.mystery0.xhu.timetable.config.store.setConfigStore
import kotlin.reflect.KMutableProperty0
import kotlin.reflect.KMutableProperty1

@Composable
fun ConfigSettingsCheckbox(
    modifier: Modifier = Modifier,
    config: KMutableProperty1<ConfigStore, Boolean>,
    scope: CoroutineScope = rememberCoroutineScope(),
    icon: @Composable (() -> Unit)? = null,
    title: @Composable () -> Unit,
    subtitle: @Composable (() -> Unit)? = null,
    onCheckedChange: suspend (Boolean) -> Unit = { },
) {
    val valueState = rememberBooleanSettingState(config.get(GlobalConfigStore))
    SettingsCheckbox(
        modifier = modifier,
        state = valueState,
        icon = icon,
        title = title,
        subtitle = subtitle,
        onCheckedChange = { newValue ->
            valueState.value = newValue
            scope.launch {
                setConfigStore { config.set(GlobalConfigStore, newValue) }
                onCheckedChange(newValue)
            }
        },
    )
}

@Composable
fun CacheSettingsCheckbox(
    modifier: Modifier = Modifier,
    config: KMutableProperty1<CacheStore, Boolean>,
    scope: CoroutineScope = rememberCoroutineScope(),
    icon: @Composable (() -> Unit)? = null,
    title: @Composable () -> Unit,
    subtitle: @Composable (() -> Unit)? = null,
    onCheckedChange: suspend (Boolean) -> Unit = { },
) {
    val valueState = rememberBooleanSettingState(config.get(GlobalCacheStore))
    SettingsCheckbox(
        modifier = modifier,
        state = valueState,
        icon = icon,
        title = title,
        subtitle = subtitle,
        onCheckedChange = { newValue ->
            valueState.value = newValue
            scope.launch {
                setConfigStore { config.set(GlobalCacheStore, newValue) }
                onCheckedChange(newValue)
            }
        },
    )
}

@Composable
fun PoemsSettingsCheckbox(
    modifier: Modifier = Modifier,
    config: KMutableProperty0<Boolean>,
    scope: CoroutineScope = rememberCoroutineScope(),
    icon: @Composable (() -> Unit)? = null,
    title: @Composable () -> Unit,
    subtitle: @Composable (() -> Unit)? = null,
    onCheckedChange: suspend (Boolean) -> Unit = { },
) {
    val valueState = rememberBooleanSettingState(config.get())
    SettingsCheckbox(
        modifier = modifier,
        state = valueState,
        icon = icon,
        title = title,
        subtitle = subtitle,
        onCheckedChange = { newValue ->
            valueState.value = newValue
            scope.launch {
                setConfigStore { config.set(newValue) }
                onCheckedChange(newValue)
            }
        },
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