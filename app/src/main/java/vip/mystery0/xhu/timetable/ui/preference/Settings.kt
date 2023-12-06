package vip.mystery0.xhu.timetable.ui.preference

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import me.zhanghai.compose.preference.Preference
import me.zhanghai.compose.preference.SwitchPreference
import me.zhanghai.compose.preference.TwoTargetSwitchPreference
import vip.mystery0.xhu.timetable.config.store.CacheStore
import vip.mystery0.xhu.timetable.config.store.ConfigStore
import vip.mystery0.xhu.timetable.config.store.GlobalCacheStore
import vip.mystery0.xhu.timetable.config.store.GlobalConfigStore
import vip.mystery0.xhu.timetable.config.store.setConfigStore
import kotlin.reflect.KMutableProperty0
import kotlin.reflect.KMutableProperty1

@Composable
fun XhuActionSettingsCheckbox(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: @Composable () -> Unit = { },
    title: @Composable () -> Unit,
    subtitle: (@Composable () -> Unit)? = null,
    checkboxEnabled: Boolean = true,
    onClick: () -> Unit,
) {
    TwoTargetSwitchPreference(
        value = checked,
        onValueChange = onCheckedChange,
        title = title,
        modifier = modifier,
        enabled = enabled,
        icon = icon,
        summary = subtitle,
        switchEnabled = checkboxEnabled,
        onClick = onClick,
    )
}

@Composable
fun XhuSettingsMenuLink(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: @Composable () -> Unit = { },
    title: @Composable () -> Unit,
    subtitle: (@Composable () -> Unit)? = null,
    onClick: () -> Unit,
) {
    Preference(
        title = title,
        modifier = modifier,
        enabled = enabled,
        icon = icon,
        summary = subtitle,
        onClick = onClick,
    )
}

@Composable
fun ConfigSettingsCheckbox(
    modifier: Modifier = Modifier,
    config: KMutableProperty1<ConfigStore, Boolean>,
    scope: CoroutineScope = rememberCoroutineScope(),
    icon: @Composable () -> Unit = { },
    title: @Composable () -> Unit,
    subtitle: @Composable (() -> Unit)? = null,
    onCheckedChange: suspend (Boolean) -> Unit = { },
) {
    SwitchPreference(
        value = config.get(GlobalConfigStore),
        onValueChange = { newValue: Boolean ->
            config.set(GlobalConfigStore, newValue)
            scope.launch {
                setConfigStore { config.set(GlobalConfigStore, newValue) }
                onCheckedChange(newValue)
            }
        },
        title = title,
        modifier = modifier,
        icon = icon,
        summary = subtitle,
    )
}

@Composable
fun CacheSettingsCheckbox(
    modifier: Modifier = Modifier,
    config: KMutableProperty1<CacheStore, Boolean>,
    scope: CoroutineScope = rememberCoroutineScope(),
    icon: @Composable () -> Unit = { },
    title: @Composable () -> Unit,
    subtitle: @Composable (() -> Unit)? = null,
    onCheckedChange: suspend (Boolean) -> Unit = { },
) {
    SwitchPreference(
        value = config.get(GlobalCacheStore),
        onValueChange = { newValue: Boolean ->
            scope.launch {
                setConfigStore { config.set(GlobalCacheStore, newValue) }
                onCheckedChange(newValue)
            }
        },
        title = title,
        modifier = modifier,
        icon = icon,
        summary = subtitle,
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
    SwitchPreference(
        value = config.get(),
        onValueChange = { newValue: Boolean ->
            scope.launch {
                setConfigStore { config.set(newValue) }
                onCheckedChange(newValue)
            }
        },
        title = title,
        modifier = modifier,
        icon = icon,
        summary = subtitle,
    )
}