package vip.mystery0.xhu.timetable.base

import androidx.lifecycle.viewModelScope
import androidx.paging.LoadState
import androidx.paging.LoadStates
import androidx.paging.PagingData
import androidx.paging.cachedIn
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlin.time.Clock
import vip.mystery0.xhu.timetable.config.store.User
import vip.mystery0.xhu.timetable.module.desc

@OptIn(ExperimentalCoroutinesApi::class)
abstract class PagingComposeViewModel<REQ, RESP : Any>(
    pageLoader: (REQ) -> Flow<PagingData<RESP>>,
) : ComposeViewModel() {
    // 分页数据
    protected val pageRequestFlow = MutableStateFlow<REQ?>(null)
    private val _pageState = pageRequestFlow
        .flatMapLatest {
            if (it == null) return@flatMapLatest flowOf(PagingData.empty())
            pageLoader(it)
        }
        .cachedIn(viewModelScope)
    val pageState: Flow<PagingData<RESP>> = _pageState

    private val _refreshing = MutableStateFlow(false)
    val refreshing: StateFlow<Boolean> = _refreshing

    protected suspend fun loadData(req: REQ) {
        _refreshing.value = true
        pageRequestFlow.emit(req)
    }

    fun handleLoadState(loadStates: LoadStates) {
        val anyLoading = arrayOf(
            loadStates.append,
            loadStates.prepend,
            loadStates.refresh
        ).filterIsInstance<LoadState.Loading>().any()
        _refreshing.value = anyLoading

        arrayOf(
            loadStates.append,
            loadStates.prepend,
            loadStates.refresh
        ).filterIsInstance<LoadState.Error>()
            .firstOrNull()
            ?.error
            ?.let {
                logger.w("handle LoadState Error", it)
                toastMessage(it.message ?: it.desc())
            }
    }
}

data class PageRequest(
    val user: User,
    val year: Int,
    val term: Int,
    val requestTime: Long = Clock.System.now().toEpochMilliseconds(),
)