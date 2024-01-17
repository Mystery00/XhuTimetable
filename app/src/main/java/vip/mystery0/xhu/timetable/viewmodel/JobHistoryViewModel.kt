package vip.mystery0.xhu.timetable.viewmodel

import android.util.Log
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import vip.mystery0.xhu.timetable.base.ComposeViewModel
import vip.mystery0.xhu.timetable.config.networkErrorHandler
import vip.mystery0.xhu.timetable.repository.JobRepo
import vip.mystery0.xhu.timetable.utils.asLocalDateTime
import vip.mystery0.xhu.timetable.utils.formatChinaDateTime
import java.time.Instant

class JobHistoryViewModel : ComposeViewModel() {
    companion object {
        private const val TAG = "JobHistoryViewModel"
    }

    private val _historyListState = MutableStateFlow(JobHistoryListState())
    val historyListState: StateFlow<JobHistoryListState> = _historyListState

    init {
        loadHistoryList()
    }

    fun loadHistoryList() {
        fun failed(message: String) {
            Log.w(TAG, "load exp score list failed, $message")
            toastMessage(message)
        }

        viewModelScope.launch(networkErrorHandler { throwable ->
            Log.w(TAG, "load exp score list failed", throwable)
            failed(throwable.message ?: throwable.javaClass.simpleName)
        }) {
            _historyListState.value = JobHistoryListState(loading = true)
            val historyList = JobRepo.fetchHistoryList().map {
                var executeTime = "-"
                if (it.executeTime != 0L) {
                    executeTime =
                        Instant.ofEpochMilli(it.executeTime).asLocalDateTime().formatChinaDateTime()
                }
                var message = "暂无"
                if (it.message.isNotBlank()) {
                    message = it.message
                }
                JobHistory(
                    jobId = it.jobId,
                    prepareExecuteTime = it.prepareExecuteTime.asLocalDateTime()
                        .formatChinaDateTime(),
                    jobTypeTitle = it.jobTypeTitle,
                    showStatus = it.success || it.failed,
                    success = it.success,
                    failed = it.failed,
                    status = it.status,
                    executeTime = executeTime,
                    message = message,
                    registrationId = it.ext.getOrDefault("registrationId", ""),
                )
            }
            _historyListState.value = JobHistoryListState(history = historyList)
        }
    }

    fun addAutoCheckScoreJob(registrationId: String) {
        viewModelScope.launch(networkErrorHandler { throwable ->
            Log.w(TAG, "add auto check score job failed", throwable)
            toastMessage(throwable.message ?: throwable.javaClass.simpleName)
        }) {
            JobRepo.addAutoCheckScoreJob(registrationId)
            loadHistoryList()
        }
    }
}

data class JobHistoryListState(
    val loading: Boolean = false,
    val history: List<JobHistory> = emptyList(),
)

data class JobHistory(
    val jobId: Long,
    val prepareExecuteTime: String,
    val jobTypeTitle: String,
    val showStatus: Boolean,
    val success: Boolean,
    val failed: Boolean,
    val status: String,
    val executeTime: String,
    val message: String,
    val registrationId: String,
)