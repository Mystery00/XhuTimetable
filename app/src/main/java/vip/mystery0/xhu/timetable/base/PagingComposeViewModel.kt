package vip.mystery0.xhu.timetable.base

import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import vip.mystery0.xhu.timetable.config.store.User

@OptIn(ExperimentalCoroutinesApi::class)
abstract class PagingComposeViewModel<REQ, RESP : Any>(
    pageLoader: (REQ) -> Flow<PagingData<RESP>>,
) : ComposeViewModel() {
    // 分页数据
    protected val pageRequestFlow = MutableStateFlow<REQ?>(null)
    private val _pageState = pageRequestFlow
        .flatMapLatest {
            if (it == null) return@flatMapLatest flowOf(PagingData.empty())
            val dataFlow = pageLoader(it)
            _refreshing.value = false
            dataFlow
        }.cachedIn(viewModelScope)
    val pageState: Flow<PagingData<RESP>> = _pageState

    private val _refreshing = MutableStateFlow(false)
    val refreshing: StateFlow<Boolean> = _refreshing

    private val _errorMessage = MutableStateFlow(Pair(System.currentTimeMillis(), ""))
    val errorMessage: StateFlow<Pair<Long, String>> = _errorMessage

    protected fun toastMessage(message: String) {
        _errorMessage.value = System.currentTimeMillis() to message
    }

    protected suspend fun loadData(req: REQ) {
        _refreshing.value = true
        pageRequestFlow.emit(req)
    }
}

data class PageRequest(
    val user: User,
    val year: Int,
    val term: Int,
    val requestTime: Long = System.currentTimeMillis(),
)