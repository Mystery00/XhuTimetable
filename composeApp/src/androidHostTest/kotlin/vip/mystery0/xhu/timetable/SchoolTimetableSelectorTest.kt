package vip.mystery0.xhu.timetable

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import vip.mystery0.xhu.timetable.config.ServerError
import vip.mystery0.xhu.timetable.config.coroutine.GlobalCoroutineExceptionHandler
import vip.mystery0.xhu.timetable.viewmodel.CampusSelectDataLoader
import vip.mystery0.xhu.timetable.viewmodel.CollegeSelectDataLoader
import vip.mystery0.xhu.timetable.viewmodel.MajorSelectDataLoader
import vip.mystery0.xhu.timetable.viewmodel.SchoolTimetableViewModel
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SchoolTimetableSelectorTest {
    @Test
    fun selectCollegeShowsServerErrorWithoutCrashingAndClearsStaleMajors() = runBlocking {
        var uncaught: Throwable? = null
        GlobalCoroutineExceptionHandler.setHandler { _, throwable ->
            uncaught = throwable
        }

        val majorLoader = MajorSelectDataLoader { collegeId ->
            when (collegeId) {
                "00" -> mapOf("软件工程" to "0001")
                "01" -> throw ServerError("研究生平台暂不支持此功能")
                else -> emptyMap()
            }
        }
        val viewModel = SchoolTimetableViewModel(
            campusSelect = CampusSelectDataLoader { mapOf("郫都校区" to "1") },
            collegeSelect = CollegeSelectDataLoader { mapOf("旧学院" to "00", "研究生学院" to "01") },
            majorSelect = majorLoader,
            errorHandlerFactory = { onError ->
                { throwable ->
                    onError(throwable)
                    true
                }
            },
        )

        viewModel.init()
        withTimeout(3_000) {
            while (viewModel.collegeSelect.select.value.size < 2) {
                delay(10)
            }
        }

        // 先加载一个有专业数据的学院，再切换到返回服务端错误的学院
        viewModel.selectCollege("00")
        withTimeout(3_000) {
            while (viewModel.majorSelect.select.value.isEmpty()) {
                delay(10)
            }
        }
        assertEquals("0001", viewModel.majorSelect.select.value.single().value)

        viewModel.selectCollege("01")
        withTimeout(3_000) {
            while (viewModel.errorMessage.value.isEmpty()) {
                delay(10)
            }
        }

        assertNull(uncaught, "服务端错误不应逃逸到全局未捕获异常处理器")
        assertEquals("研究生平台暂不支持此功能", viewModel.errorMessage.value)
        assertTrue(viewModel.majorSelect.select.value.isEmpty(), "切换学院加载失败时应清空旧的专业选项")
    }

    @Test
    fun initShowsServerErrorWithoutCrashingWhenSelectorUnsupported() = runBlocking {
        var uncaught: Throwable? = null
        GlobalCoroutineExceptionHandler.setHandler { _, throwable ->
            uncaught = throwable
        }

        val viewModel = SchoolTimetableViewModel(
            campusSelect = CampusSelectDataLoader { throw ServerError("研究生平台暂不支持此功能") },
            collegeSelect = CollegeSelectDataLoader { emptyMap() },
            majorSelect = MajorSelectDataLoader { emptyMap() },
            errorHandlerFactory = { onError ->
                { throwable ->
                    onError(throwable)
                    true
                }
            },
        )

        viewModel.init()
        withTimeout(3_000) {
            while (viewModel.errorMessage.value.isEmpty()) {
                delay(10)
            }
        }

        assertNull(uncaught, "初始化筛选选项失败不应触发全局未捕获异常")
        assertEquals("研究生平台暂不支持此功能", viewModel.errorMessage.value)
    }
}
