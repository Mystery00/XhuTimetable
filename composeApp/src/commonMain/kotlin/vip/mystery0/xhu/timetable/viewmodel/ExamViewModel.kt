package vip.mystery0.xhu.timetable.viewmodel

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kotlin.time.Clock
import kotlinx.datetime.LocalDate
import vip.mystery0.xhu.timetable.base.PagingComposeViewModel
import vip.mystery0.xhu.timetable.base.UserSelectDataLoader
import vip.mystery0.xhu.timetable.config.networkErrorHandler
import vip.mystery0.xhu.timetable.config.store.User
import vip.mystery0.xhu.timetable.module.desc
import vip.mystery0.xhu.timetable.repository.ExamRepo
import vip.mystery0.xhu.timetable.ui.theme.XhuColor

class ExamViewModel : PagingComposeViewModel<PageRequest, Exam>(
    {
        ExamRepo.getExamListStream(it.user)
    }
) {
    val userSelect = UserSelectDataLoader()

    fun init() {
        viewModelScope.launch {
            userSelect.init()
            loadExamList()
        }
    }

    fun loadExamList() {
        fun failed(message: String) {
            logger.w("load exam list failed, $message")
            toastMessage(message)
        }
        viewModelScope.launch(networkErrorHandler { throwable ->
            logger.w("load exam list failed", throwable)
            failed(throwable.message ?: throwable.desc())
        }) {
            val selectedUser = userSelect.getSelectedUser()
            if (selectedUser == null) {
                failed("选择用户为空，请重新选择")
                return@launch
            }
            loadData(PageRequest(selectedUser))
        }
    }

    fun selectUser(studentId: String) {
        viewModelScope.launch {
            userSelect.setSelected(studentId)
        }
    }
}

data class PageRequest(
    val user: User,
    val requestTime: Long = Clock.System.now().toEpochMilliseconds(),
)

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