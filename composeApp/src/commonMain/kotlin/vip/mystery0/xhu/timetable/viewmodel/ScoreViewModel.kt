package vip.mystery0.xhu.timetable.viewmodel

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import vip.mystery0.xhu.timetable.base.PageRequest
import vip.mystery0.xhu.timetable.base.PagingComposeViewModel
import vip.mystery0.xhu.timetable.base.TermSelectDataLoader
import vip.mystery0.xhu.timetable.base.UserSelectDataLoader
import vip.mystery0.xhu.timetable.base.YearSelectDataLoader
import vip.mystery0.xhu.timetable.config.coroutine.safeLaunch
import vip.mystery0.xhu.timetable.config.networkErrorHandler
import vip.mystery0.xhu.timetable.model.response.ScoreGpaResponse
import vip.mystery0.xhu.timetable.model.response.ScoreResponse
import vip.mystery0.xhu.timetable.module.desc
import vip.mystery0.xhu.timetable.push.PushPermissionState
import vip.mystery0.xhu.timetable.push.pushManager
import vip.mystery0.xhu.timetable.repository.JobRepo
import vip.mystery0.xhu.timetable.repository.ScoreRepo

class ScoreViewModel : PagingComposeViewModel<PageRequest, ScoreResponse>(
    {
        ScoreRepo.getScoreListStream(it.user, it.year, it.term)
    }
) {
    val userSelect = UserSelectDataLoader()
    val yearSelect = YearSelectDataLoader()
    val termSelect = TermSelectDataLoader()

    private val _scoreGpa = MutableStateFlow<ScoreGpaResponse?>(null)
    val scoreGpa: StateFlow<ScoreGpaResponse?> = _scoreGpa

    private val _autoScoreState = MutableStateFlow(AutoScoreSubscribeState())
    val autoScoreState: StateFlow<AutoScoreSubscribeState> = _autoScoreState

    fun init() {
        viewModelScope.safeLaunch {
            userSelect.init()
            yearSelect.init()
            termSelect.init()
            loadScoreList()
            loadScoreGpa()
            loadAutoScoreStatus()
        }
    }

    fun loadScoreList() {
        fun failed(message: String) {
            logger.w("load score list failed, $message")
            toastMessage(message)
        }
        viewModelScope.safeLaunch(onException = networkErrorHandler { throwable ->
            logger.w("load score list failed", throwable)
            failed(throwable.desc())
        }) {
            val selectedUser = userSelect.getSelectedUser()
            if (selectedUser == null) {
                failed("选择用户为空，请重新选择")
                return@safeLaunch
            }
            val year = yearSelect.getSelectedYear()
            val term = termSelect.getSelectedTerm()
            loadData(PageRequest(selectedUser, year, term))
        }
    }

    fun loadScoreGpa() {
        viewModelScope.safeLaunch(onException = networkErrorHandler { throwable ->
            logger.w("load score gpa failed", throwable)
        }) {
            val selectedUser = userSelect.getSelectedUser() ?: return@safeLaunch
            val year = yearSelect.getSelectedYear()
            val term = termSelect.getSelectedTerm()
            _scoreGpa.value = ScoreRepo.getGpa(selectedUser, year, term)
        }
    }

    fun selectUser(studentId: String) {
        viewModelScope.safeLaunch {
            userSelect.setSelected(studentId)
        }
    }

    fun selectYear(year: Int) {
        viewModelScope.safeLaunch {
            yearSelect.setSelected(year)
        }
    }

    fun selectTerm(term: Int) {
        viewModelScope.safeLaunch {
            termSelect.setSelected(term)
        }
    }

    fun loadAutoScoreStatus() {
        viewModelScope.safeLaunch(onException = networkErrorHandler { throwable ->
            logger.w("load auto score status failed", throwable)
            _autoScoreState.value = AutoScoreSubscribeState(errorMessage = throwable.desc())
        }) {
            _autoScoreState.value = _autoScoreState.value.copy(loading = true, errorMessage = "")
            val response = JobRepo.fetchAutoScoreJobStatus()
            _autoScoreState.value = AutoScoreSubscribeState(
                hasActiveTask = response.hasActiveTask,
                status = response.status,
                expireDate = response.expireDate?.toString(),
                nextCheckTime = response.nextCheckTime,
                lastCheckTime = response.lastCheckTime,
                lastCheckResult = response.lastCheckResult,
            )
        }
    }

    fun startAutoScoreSubscribe() {
        viewModelScope.safeLaunch(onException = networkErrorHandler { throwable ->
            logger.w("start auto score job failed", throwable)
            toastMessage(throwable.desc())
            _autoScoreState.value = _autoScoreState.value.copy(loading = false)
        }) {
            _autoScoreState.value = _autoScoreState.value.copy(loading = true, errorMessage = "")
            val permissionState = pushManager.requestPermissionIfNeeded()
            if (permissionState == PushPermissionState.DENIED) {
                _autoScoreState.value = _autoScoreState.value.copy(loading = false)
                toastMessage("通知权限未开启，无法订阅成绩变更提醒")
                return@safeLaunch
            }
            JobRepo.startAutoScoreJob()
            toastMessage("成绩变更提醒已开启")
            loadAutoScoreStatus()
        }
    }

    fun stopAutoScoreSubscribe() {
        viewModelScope.safeLaunch(onException = networkErrorHandler { throwable ->
            logger.w("stop auto score job failed", throwable)
            toastMessage(throwable.desc())
            _autoScoreState.value = _autoScoreState.value.copy(loading = false)
        }) {
            _autoScoreState.value = _autoScoreState.value.copy(loading = true, errorMessage = "")
            JobRepo.stopAutoScoreJob()
            toastMessage("成绩变更提醒已取消")
            loadAutoScoreStatus()
        }
    }
}

data class AutoScoreSubscribeState(
    val loading: Boolean = false,
    val hasActiveTask: Boolean = false,
    val status: String? = null,
    val expireDate: String? = null,
    val nextCheckTime: String? = null,
    val lastCheckTime: String? = null,
    val lastCheckResult: String? = null,
    val errorMessage: String = "",
)
