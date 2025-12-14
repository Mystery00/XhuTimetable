package vip.mystery0.xhu.timetable.repository

import androidx.compose.ui.graphics.Color
import io.ktor.util.collections.ConcurrentSet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import vip.mystery0.xhu.timetable.config.Customisable
import vip.mystery0.xhu.timetable.db.dao.CourseColorDao
import vip.mystery0.xhu.timetable.db.dao.CourseDao
import vip.mystery0.xhu.timetable.db.dao.CustomCourseDao
import vip.mystery0.xhu.timetable.db.dao.ExperimentCourseDao
import vip.mystery0.xhu.timetable.model.entity.CourseColor
import vip.mystery0.xhu.timetable.ui.theme.ColorPool
import vip.mystery0.xhu.timetable.utils.parseColorHexString

object CourseColorRepo : KoinComponent {
    private val courseDao: CourseDao by inject()
    private val experimentCourseDao: ExperimentCourseDao by inject()
    private val customCourseDao: CustomCourseDao by inject()
    private val courseColorDao: CourseColorDao by inject()

    suspend fun getRawCourseColorList(): Map<String, Color> {
        val colorList = withContext(Dispatchers.IO) {
            courseColorDao.queryAllCourseColorList()
        }
        return withContext(Dispatchers.Default) {
            val map = HashMap<String, Color>(colorList.size)
            colorList.forEach {
                map[it.courseName] = it.color.parseColorHexString()
            }
            map
        }
    }

    suspend fun getCourseColorList(keywords: String): List<Pair<String, Customisable<Color>>> {
        val courseSet = ConcurrentSet<String>()
        withContext(Dispatchers.IO) {
            if (keywords.isBlank()) {
                courseSet.addAll(courseDao.queryDistinctCourseByUsernameAndTerm())
            } else {
                courseSet.addAll(courseDao.queryDistinctCourseByKeywordsAndUsernameAndTerm("%${keywords}%"))
            }
        }
        withContext(Dispatchers.IO) {
            if (keywords.isBlank()) {
                courseSet.addAll(experimentCourseDao.queryDistinctCourseByUsernameAndTerm())
            } else {
                courseSet.addAll(
                    experimentCourseDao.queryDistinctCourseByKeywordsAndUsernameAndTerm(
                        "%${keywords}%"
                    )
                )
            }
        }
        withContext(Dispatchers.IO) {
            if (keywords.isBlank()) {
                courseSet.addAll(customCourseDao.queryDistinctCourseByUsernameAndTerm())
            } else {
                courseSet.addAll(customCourseDao.queryDistinctCourseByKeywordsAndUsernameAndTerm("%${keywords}%"))
            }
        }
        val colorList = withContext(Dispatchers.IO) {
            courseColorDao.queryAllCourseColorList()
        }
        return withContext(Dispatchers.Default) {
            val map = HashMap<String, Color>(colorList.size)
            colorList.forEach {
                map[it.courseName] = it.color.parseColorHexString()
            }
            courseSet.map {
                val second = if (map.containsKey(it)) {
                    Customisable.custom(map[it]!!)
                } else {
                    Customisable.default(ColorPool.hash(it))
                }
                Pair(it, second)
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
            saved?.color?.parseColorHexString() ?: ColorPool.hash(courseName)
        }
    }
}