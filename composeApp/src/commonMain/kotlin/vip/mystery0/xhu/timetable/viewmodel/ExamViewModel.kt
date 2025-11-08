package vip.mystery0.xhu.timetable.viewmodel

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewModelScope
import kotlinx.datetime.LocalDate
import vip.mystery0.xhu.timetable.base.PageRequest
import vip.mystery0.xhu.timetable.base.PagingComposeViewModel
import vip.mystery0.xhu.timetable.base.TermSelectDataLoader
import vip.mystery0.xhu.timetable.base.UserSelectDataLoader
import vip.mystery0.xhu.timetable.base.YearSelectDataLoader
import vip.mystery0.xhu.timetable.config.coroutine.safeLaunch
import vip.mystery0.xhu.timetable.config.networkErrorHandler
import vip.mystery0.xhu.timetable.module.desc
import vip.mystery0.xhu.timetable.repository.ExamRepo
import vip.mystery0.xhu.timetable.ui.theme.XhuColor

class ExamViewModel : PagingComposeViewModel<PageRequest, Exam>(
    {
        ExamRepo.getExamListStream(it.user, it.year, it.term)
    }
) {
    val userSelect = UserSelectDataLoader()
    val yearSelect = YearSelectDataLoader()
    val termSelect = TermSelectDataLoader()

    fun init() {
        viewModelScope.safeLaunch {
            userSelect.init()
            yearSelect.init()
            termSelect.init()
            loadExamList()
        }
    }

    fun loadExamList() {
        fun failed(message: String) {
            logger.w("load exam list failed, $message")
            toastMessage(message)
        }
        viewModelScope.safeLaunch(onException = networkErrorHandler { throwable ->
            logger.w("load exam list failed", throwable)
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
}

@Immutable
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