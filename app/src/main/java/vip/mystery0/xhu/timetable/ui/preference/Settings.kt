package vip.mystery0.xhu.timetable.ui.preference

import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchColors
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import com.alorma.compose.settings.storage.base.rememberBooleanSettingState
import com.alorma.compose.settings.ui.SettingsMenuLink
import com.alorma.compose.settings.ui.SettingsSwitch
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
fun XhuActionSettingsCheckbox(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: @Composable () -> Unit = {},
    title: @Composable () -> Unit,
    subtitle: (@Composable () -> Unit)? = null,
    checkboxColors: SwitchColors = SwitchDefaults.colors(),
    onCheckedChange: ((Boolean) -> Unit)? = null,
    checkboxEnabled: Boolean = true,
    checked: Boolean,
    onClick: () -> Unit,
) {
    SettingsMenuLink(
        modifier,
        enabled,
        icon,
        title,
        subtitle,
        action = @Composable {
            Switch(
                enabled = checkboxEnabled,
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = checkboxColors,
            )
        },
        onClick,
    )
}

@Composable
fun XhuSettingsMenuLink(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: @Composable () -> Unit = {},
    title: @Composable () -> Unit,
    subtitle: (@Composable () -> Unit)? = null,
    onClick: () -> Unit,
) {
    SettingsMenuLink(
        modifier = modifier,
        enabled = enabled,
        icon = icon,
        title = title,
        subtitle = subtitle,
        onClick = onClick,
    )
}

@Composable
fun ConfigSettingsCheckbox(
    modifier: Modifier = Modifier,
    config: KMutableProperty1<ConfigStore, Boolean>,
    scope: CoroutineScope = rememberCoroutineScope(),
    icon: @Composable () -> Unit = {},
    title: @Composable () -> Unit,
    subtitle: @Composable (() -> Unit)? = null,
    onCheckedChange: suspend (Boolean) -> Unit = { },
) {
    val valueState = rememberBooleanSettingState(config.get(GlobalConfigStore))
    SettingsSwitch(
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
    icon: @Composable () -> Unit = {},
    title: @Composable () -> Unit,
    subtitle: @Composable (() -> Unit)? = null,
    onCheckedChange: suspend (Boolean) -> Unit = { },
) {
    val valueState = rememberBooleanSettingState(config.get(GlobalCacheStore))
    SettingsSwitch(
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
    icon: @Composable () -> Unit = { },
    title: @Composable () -> Unit,
    subtitle: @Composable (() -> Unit)? = null,
    onCheckedChange: suspend (Boolean) -> Unit = { },
) {
    val valueState = rememberBooleanSettingState(config.get())
    SettingsSwitch(
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