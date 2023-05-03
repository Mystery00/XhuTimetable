package vip.mystery0.xhu.timetable.repository

import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.java.KoinJavaComponent
import vip.mystery0.xhu.timetable.config.runOnCpu
import vip.mystery0.xhu.timetable.config.runOnIo
import vip.mystery0.xhu.timetable.model.entity.CourseColor
import vip.mystery0.xhu.timetable.repository.db.dao.CourseColorDao
import vip.mystery0.xhu.timetable.repository.db.dao.CourseDao
import vip.mystery0.xhu.timetable.ui.theme.ColorPool

object CourseColorRepo : KoinComponent {
    private val courseDao: CourseDao by inject()
    private val courseColorDao: CourseColorDao by inject()

    suspend fun getRawCourseColorList(): Map<String, Color> {
        val colorList = withContext(Dispatchers.IO) {
            courseColorDao.queryAllCourseColorList()
        }
        return withContext(Dispatchers.Default) {
            val map = HashMap<String, Color>(colorList.size)
            colorList.forEach {
                val color = android.graphics.Color.parseColor(it.color)
                map[it.courseName] = Color(color)
            }
            map
        }
    }

    suspend fun getCourseColorList(keywords: String): List<Pair<String, Color>> {
        val courseList = withContext(Dispatchers.IO) {
            if (keywords.isBlank()) {
                courseDao.queryDistinctCourseByUsernameAndTerm()
            } else {
                courseDao.queryDistinctCourseByKeywordsAndUsernameAndTerm("%${keywords}%")
            }
        }
        val colorList = withContext(Dispatchers.IO) {
            courseColorDao.queryAllCourseColorList()
        }
        return withContext(Dispatchers.Default) {
            val map = HashMap<String, Color>(colorList.size)
            colorList.forEach {
                val color = android.graphics.Color.parseColor(it.color)
                map[it.courseName] = Color(color)
            }
            courseList.map {
                Pair(it, map[it] ?: ColorPool.hash(it))
            }
        }
    }

    suspend fun updateCourseColor(courseName: String, color: String?) {
        val saved = withContext(Dispatchers.IO) {
            courseColorDao.selectCourseColor(courseName)
        }
        if (saved != null) {
            if (color == null) {
                withContext(Dispatchers.IO) {
                    courseColorDao.delete(saved)
                }
            } else {
                saved.color = color
                withContext(Dispatchers.IO) {
                    courseColorDao.update(saved)
                }
            }
        } else {
            if (color != null) {
                withContext(Dispatchers.IO) {
                    courseColorDao.save(CourseColor(courseName, color))
                }
            }
        }
    }

    suspend fun deleteAllCourseColor() {
        withContext(Dispatchers.IO) {
            courseColorDao.queryAllCourseColorList().forEach {
                courseColorDao.delete(it)
            }
        }
    }

    suspend fun getCourseColorByName(courseName: String): Color {
        val saved = withContext(Dispatchers.IO) { courseColorDao.selectCourseColor(courseName) }
        return withContext(Dispatchers.Default) {
            if (saved != null) {
                val color = android.graphics.Color.parseColor(saved.color)
                Color(color)
            } else {
                ColorPool.hash(courseName)
            }
        }
    }
}