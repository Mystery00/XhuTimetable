package vip.mystery0.xhu.timetable.ui.widget.state

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.glance.state.GlanceStateDefinition
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import vip.mystery0.xhu.timetable.repository.getCurrentWeek
import vip.mystery0.xhu.timetable.repository.getTimeTitle
import vip.mystery0.xhu.timetable.repository.getWeekCourse
import vip.mystery0.xhu.timetable.ui.widget.widgetDataStoreFile
import vip.mystery0.xhu.timetable.viewmodel.CourseSheet
import java.io.File
import java.time.DayOfWeek
import java.time.LocalDate

class WeekGlanceStateDefinition : GlanceStateDefinition<WeekCourseStateGlance> {
    override suspend fun getDataStore(
        context: Context,
        fileKey: String
    ): DataStore<WeekCourseStateGlance> = WeekCourseDataStore()

    override fun getLocation(context: Context, fileKey: String): File =
        context.widgetDataStoreFile(fileKey)
}

class WeekCourseDataStore : DataStore<WeekCourseStateGlance> {
    private val dataFlow = MutableStateFlow(WeekCourseStateGlance.EMPTY)

    override val data: Flow<WeekCourseStateGlance> = flow {
        val currentWeek = getCurrentWeek()
        val weekCourseList = getWeekCourse(currentWeek)
        val timeTitle = getTimeTitle()
        val now = LocalDate.now()
        val startDate = now.minusDays(now.dayOfWeek.value.toLong() - 1)
        emit(
            WeekCourseStateGlance(
                timeTitle,
                now,
                currentWeek,
                weekCourseList,
                startDate,
            )
        )
    }

    override suspend fun updateData(transform: suspend (t: WeekCourseStateGlance) -> WeekCourseStateGlance): WeekCourseStateGlance =
        dataFlow.value
}

data class WeekCourseStateGlance(
    val timeTitle: String,
    val date: LocalDate,
    val currentWeek: Int,
    val weekCourseList: List<List<CourseSheet>>,
    val startDate: LocalDate,
    val week: DayOfWeek = date.dayOfWeek,
) {
    val hasData: Boolean
        get() = weekCourseList.isNotEmpty()

    companion object {
        val EMPTY = WeekCourseStateGlance(
            "数据初始化中……",
            LocalDate.now(),
            0,
            emptyList(),
            LocalDate.now(),
        )
    }
}