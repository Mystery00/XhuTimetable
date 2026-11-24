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
import vip.mystery0.xhu.timetable.widget.WidgetSnapshotRepo
import java.io.File
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

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
        val now = LocalDate.now()
        val nowMillis = System.currentTimeMillis()
        val snapshot = withContext(Dispatchers.Default) { WidgetSnapshotRepo.generate() }
        val timeTitle = withContext(Dispatchers.Default) { WidgetRepo.calculateDateTitle(false) }

        if (snapshot.state == "loggedOut") {
            emit(
                TodayCourseStateGlance(
                    timeTitle = timeTitle,
                    date = now,
                    currentWeek = 0,
                    todayCourseList = emptyList(),
                    isLoggedOut = true,
                    nowMillis = nowMillis,
                )
            )
            return@flow
        }

        val todayKey = now.toString()
        val snapshotDay = snapshot.days.firstOrNull { it.date == todayKey }
        val currentWeek = snapshotDay?.week ?: withContext(Dispatchers.Default) { WidgetRepo.calculateWeek() }
        val items = snapshotDay?.items?.mapIndexed { index, item ->
            CourseGlance(
                courseId = index.toLong(),
                courseName = item.title,
                location = item.location,
                time = item.timeText,
                color = Color(item.colorArgb.toInt()),
                startMillis = item.startMillis,
                endMillis = item.endMillis,
                isThing = item.kind == "thing",
            )
        } ?: withContext(Dispatchers.Default) {
            WidgetRepo.getTodayList(currentWeek).mapIndexed { index, item ->
                CourseGlance(
                    courseId = index.toLong(),
                    courseName = item.title,
                    location = item.location,
                    time = item.showTime.joinToString("–"),
                    color = item.color,
                    startMillis = item.startTime,
                    endMillis = item.startTime + 90 * 60 * 1000L,
                    isThing = false,
                )
            }
        }

        emit(
            TodayCourseStateGlance(
                timeTitle = timeTitle,
                date = now,
                currentWeek = currentWeek,
                todayCourseList = items,
                isLoggedOut = false,
                nowMillis = nowMillis,
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
    val isLoggedOut: Boolean = false,
    val nowMillis: Long = System.currentTimeMillis(),
) {
    val hasData: Boolean
        get() = todayCourseList.isNotEmpty()

    val hasCourseToday: Boolean
        get() = todayCourseList.any { !it.isThing }

    val remainingItems: List<CourseGlance>
        get() = todayCourseList.filter { it.isThing || it.endMillis > nowMillis }

    val nextCourse: CourseGlance?
        get() = todayCourseList.firstOrNull { !it.isThing && it.endMillis > nowMillis }

    val dateLabel: String
        get() {
            val weekName = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.CHINESE)
            return "${date.monthValue}月${date.dayOfMonth}日 $weekName"
        }

    val weekBadgeText: String
        get() = when {
            currentWeek <= 0 -> "未开学"
            currentWeek > 20 -> "已结课"
            else -> "第${currentWeek}周"
        }

    companion object {
        val EMPTY = TodayCourseStateGlance("数据加载中…", LocalDate.now(), 0, emptyList())
    }
}

data class CourseGlance(
    val courseId: Long,
    val courseName: String,
    val location: String,
    val time: String,
    val color: Color,
    val startMillis: Long = 0L,
    val endMillis: Long = 0L,
    val isThing: Boolean = false,
) {
    fun isOngoing(nowMillis: Long): Boolean = !isThing && startMillis in 1..nowMillis && endMillis > nowMillis
}
