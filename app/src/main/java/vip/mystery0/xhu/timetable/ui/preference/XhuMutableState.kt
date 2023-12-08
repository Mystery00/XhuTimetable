package vip.mystery0.xhu.timetable.ui.preference

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import vip.mystery0.xhu.timetable.config.store.CacheStore
import vip.mystery0.xhu.timetable.config.store.ConfigStore
import vip.mystery0.xhu.timetable.config.store.GlobalCacheStore
import vip.mystery0.xhu.timetable.config.store.GlobalConfigStore
import vip.mystery0.xhu.timetable.config.store.PoemsStore
import kotlin.reflect.KMutableProperty0
import kotlin.reflect.KMutableProperty1

@Composable
fun <T> rememberCacheState(
    property: KMutableProperty1<CacheStore, T>,
    onChange: ((T) -> Unit)? = null,
): MutableState<T> =
    remember {
        XhuMutableState(
            store = GlobalCacheStore,
            setter = property::set,
            getter = property::get,
            onChange = onChange,
        )
    }

@Composable
fun <T> rememberConfigState(
    property: KMutableProperty1<ConfigStore, T>,
    onChange: ((T) -> Unit)? = null,
): MutableState<T> =
    remember {
        XhuMutableState(
            store = GlobalConfigStore,
            setter = property::set,
            getter = property::get,
            onChange = onChange,
        )
    }

@Composable
fun <T> rememberPoemsState(
    property: KMutableProperty0<T>,
    onChange: ((T) -> Unit)? = null,
): MutableState<T> =
    remember {
        XhuMutableState(
            store = PoemsStore,
            setter = { property.set(it) },
            getter = { property.get() },
            onChange = onChange,
        )
    }

internal class XhuMutableState<S : Any, T>(
    private val store: S,
    private val setter: S.(T) -> Unit,
    private val getter: (S) -> T,
    private val onChange: ((T) -> Unit)? = null,
    private val mutableState: MutableState<T> = mutableStateOf(getter(store)),
) : MutableState<T> by mutableState {
    override var value: T
        get() = component1()
        set(value) {
            component2().invoke(value)
        }

    override fun component1(): T = mutableState.component1()

    override fun component2(): (T) -> Unit = { newValue ->
        setter(store, newValue)
        mutableState.component2().invoke(newValue)
        onChange?.invoke(newValue)
    }
}