package vip.mystery0.xhu.timetable.repository

import androidx.compose.ui.graphics.Color
import org.koin.java.KoinJavaComponent
import vip.mystery0.xhu.timetable.config.runOnCpu
import vip.mystery0.xhu.timetable.config.runOnIo
import vip.mystery0.xhu.timetable.model.entity.CourseColor
import vip.mystery0.xhu.timetable.repository.db.dao.CourseColorDao
import vip.mystery0.xhu.timetable.repository.db.dao.CourseDao
import vip.mystery0.xhu.timetable.ui.theme.ColorPool

private val courseDao by lazy {
    KoinJavaComponent.get<CourseDao>(CourseDao::class.java)
}
private val courseColorDao by lazy {
    KoinJavaComponent.get<CourseColorDao>(CourseColorDao::class.java)
}

suspend fun getRawCourseColorList(): Map<String, Color> {
    val colorList = runOnIo { courseColorDao.queryAllCourseColorList() }
    return runOnCpu {
        val map = HashMap<String, Color>(colorList.size)
        colorList.forEach {
            val color = android.graphics.Color.parseColor(it.color)
            map[it.courseName] = Color(color)
        }
        map
    }
}

suspend fun getCourseColorList(keywords: String): List<Pair<String, Color>> {
    val courseList = runOnIo {
        if (keywords.isBlank()) {
            courseDao.queryDistinctCourseByUsernameAndTerm()
        } else {
            courseDao.queryDistinctCourseByKeywordsAndUsernameAndTerm("%${keywords}%")
        }
    }
    val colorList = runOnIo { courseColorDao.queryAllCourseColorList() }
    return runOnCpu {
        val map = HashMap<String, Color>(colorList.size)
        colorList.forEach {
            val color = android.graphics.Color.parseColor(it.color)
            map[it.courseName] = Color(color)
        }
        courseList.map {
            Pair(it.courseName, map[it.courseName] ?: ColorPool.hash(it.courseName))
        }
    }
}

suspend fun updateCourseColor(courseName: String, color: String?) {
    runOnIo {
        val saved = courseColorDao.selectCourseColor(courseName)
        if (saved != null) {
            if (color == null) {
                courseColorDao.delete(saved)
            } else {
                saved.color = color
                courseColorDao.update(saved)
            }
        } else {
            if (color != null) {
                courseColorDao.save(CourseColor(courseName, color))
            }
        }
    }
}

suspend fun deleteAllCourseColor() {
    runOnIo {
        courseColorDao.queryAllCourseColorList().forEach {
            courseColorDao.delete(it)
        }
    }
}

suspend fun getCourseColorByName(courseName: String): Color {
    val saved = courseColorDao.selectCourseColor(courseName)
    return if (saved != null) {
        val color = android.graphics.Color.parseColor(saved.color)
        Color(color)
    } else {
        ColorPool.hash(courseName)
    }
}