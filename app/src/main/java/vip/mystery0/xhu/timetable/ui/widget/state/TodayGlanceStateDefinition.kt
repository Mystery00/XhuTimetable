package vip.mystery0.xhu.timetable.ui.widget.state

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.datastore.core.DataStore
import androidx.glance.state.GlanceStateDefinition
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import vip.mystery0.xhu.timetable.repository.WidgetRepo
import vip.mystery0.xhu.timetable.ui.widget.widgetDataStoreFile
import java.io.File
import java.time.DayOfWeek
import java.time.LocalDate

class TodayGlanceStateDefinition : GlanceStateDefinition<TodayCourseStateGlance> {
    override suspend fun getDataStore(
        context: Context,
        fileKey: String
    ): DataStore<TodayCourseStateGlance> = TodayCourseDataStore()

    override fun getLocation(context: Context, fileKey: String): File =
        context.widgetDataStoreFile(fileKey)
}

class TodayCourseDataStore : DataStore<TodayCourseStateGlance> {
    private val dataFlow = MutableStateFlow(TodayCourseStateGlance.EMPTY)

    override val data: Flow<TodayCourseStateGlance> = flow {
        val currentWeek = withContext(Dispatchers.Default) { WidgetRepo.calculateWeek() }
        val todayCourseList =
            withContext(Dispatchers.Default) { WidgetRepo.getTodayList(currentWeek) }
        val timeTitle = withContext(Dispatchers.Default) { WidgetRepo.calculateDateTitle(false) }
        emit(
            TodayCourseStateGlance(
                timeTitle,
                LocalDate.now(),
                currentWeek,
                todayCourseList.mapIndexed { index, item ->
                    CourseGlance(
                        index.toLong(),
                        item.title,
                        item.location,
                        item.showTime.joinToString("\n"),
                        item.color,
                    )
                },
            )
        )
    }

    override suspend fun updateData(transform: suspend (t: TodayCourseStateGlance) -> TodayCourseStateGlance): TodayCourseStateGlance =
        dataFlow.value
}

data class TodayCourseStateGlance(
    val timeTitle: String,
    val date: LocalDate,
    val currentWeek: Int,
    val todayCourseList: List<CourseGlance>,
    val week: DayOfWeek = date.dayOfWeek,
) {
    val hasData: Boolean
        get() = todayCourseList.isNotEmpty()

    companion object {
        val EMPTY = TodayCourseStateGlance("数据初始化中……", LocalDate.now(), 0, emptyList())
    }
}

data class CourseGlance(
    val courseId: Long,
    val courseName: String,
    val location: String,
    val time: String,
    val color: Color,
)