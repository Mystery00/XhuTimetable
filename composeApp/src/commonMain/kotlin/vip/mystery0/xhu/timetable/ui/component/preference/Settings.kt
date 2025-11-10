package vip.mystery0.xhu.timetable.ui.component.preference

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import kotlinx.coroutines.CoroutineScope
import me.zhanghai.compose.preference.Preference
import me.zhanghai.compose.preference.SwitchPreference
import me.zhanghai.compose.preference.TwoTargetSwitchPreference
import vip.mystery0.xhu.timetable.config.coroutine.safeLaunch
import vip.mystery0.xhu.timetable.config.store.CacheStore
import vip.mystery0.xhu.timetable.config.store.ConfigStore
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
    onClick: (() -> Unit)? = null,
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
    enabled: Boolean = true,
    config: KMutableProperty1<ConfigStore, Boolean>,
    scope: CoroutineScope = rememberCoroutineScope(),
    icon: @Composable () -> Unit = { },
    title: @Composable () -> Unit,
    subtitle: @Composable (() -> Unit)? = null,
    onCheckedChange: suspend (Boolean) -> Unit = { },
) {
    val state = rememberConfigState(
        property = config,
        onChange = {
            scope.safeLaunch {
                setConfigStore { config.set(this, it) }
                onCheckedChange(it)
            }
        })
    SwitchPreference(
        state = state,
        title = title,
        enabled = enabled,
        modifier = modifier,
        icon = icon,
        summary = subtitle,
    )
}

@Composable
fun CacheSettingsCheckbox(
    modifier: Modifier = Modifier,
    config: KMutableProperty1<CacheStore, Boolean>,
    icon: @Composable () -> Unit = { },
    title: @Composable () -> Unit,
    subtitle: @Composable (() -> Unit)? = null,
) {
    val state = rememberCacheState(property = config)
    SwitchPreference(
        state = state,
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
    icon: @Composable () -> Unit = { },
    title: @Composable () -> Unit,
    subtitle: @Composable (() -> Unit)? = null,
    onCheckedChange: (Boolean) -> Unit = { },
) {
    val state = rememberPoemsState(property = config) {
        onCheckedChange(it)
    }
    SwitchPreference(
        state = state,
        title = title,
        modifier = modifier,
        icon = icon,
        summary = subtitle,
    )
}