package vip.mystery0.xhu.timetable.viewmodel

import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import vip.mystery0.xhu.timetable.base.PagingComposeViewModel
import vip.mystery0.xhu.timetable.base.UserSelect
import vip.mystery0.xhu.timetable.config.networkErrorHandler
import vip.mystery0.xhu.timetable.config.store.User
import vip.mystery0.xhu.timetable.repository.ExamRepo
import vip.mystery0.xhu.timetable.ui.theme.XhuColor
import java.time.LocalDate

class ExamViewModel : PagingComposeViewModel<User, Exam>(
    {
        ExamRepo.getExamListStream(it)
    }
) {
    companion object {
        private const val TAG = "ExamViewModel"
    }

    private val _userSelect = MutableStateFlow<List<UserSelect>>(emptyList())
    val userSelect: StateFlow<List<UserSelect>> = _userSelect

    init {
        viewModelScope.launch {
            _userSelect.value = initUserSelect()
            loadExamList()
        }
    }

    fun loadExamList() {
        fun failed(message: String) {
            Log.w(TAG, "load exam list failed, $message")
            toastMessage(message)
        }
        viewModelScope.launch(networkErrorHandler { throwable ->
            Log.w(TAG, "load exam list failed", throwable)
            failed(throwable.message ?: throwable.javaClass.simpleName)
        }) {
            val selectedUser = getSelectedUser(_userSelect.value)
            if (selectedUser == null) {
                failed("选择用户为空，请重新选择")
                return@launch
            }
            loadData(selectedUser)
        }
    }

    fun selectUser(studentId: String) {
        viewModelScope.launch {
            val (selectList, _) = setSelectedUser(_userSelect.value, studentId)
            _userSelect.value = selectList
        }
    }
}

data class Exam(
    val courseColor: Color,
    val date: LocalDate,
    val dateString: String,
    val seatNo: String,
    val courseName: String,
    val examName: String,
    val location: String,
    val time: String,
    val examRegion: String,
    val examStatus: ExamStatus,
    val showText: String,
)

enum class ExamStatus(
    val index: Int,
    val title: String,
    val color: Color,
    val strokeWidth: Int,
) {
    BEFORE(1, "未开始", XhuColor.Status.beforeColor, 2),
    IN(0, "进行中", XhuColor.Status.inColor, 3),
    AFTER(2, "已结束", XhuColor.Status.afterColor, 1),
}