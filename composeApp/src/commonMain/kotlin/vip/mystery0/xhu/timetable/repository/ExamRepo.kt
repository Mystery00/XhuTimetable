package vip.mystery0.xhu.timetable.repository

import androidx.annotation.AnyThread
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDate
import kotlinx.datetime.format
import org.koin.core.component.inject
import vip.mystery0.xhu.timetable.api.ExamApi
import vip.mystery0.xhu.timetable.base.BaseDataRepo
import vip.mystery0.xhu.timetable.base.buildPageSource
import vip.mystery0.xhu.timetable.config.store.Formatter
import vip.mystery0.xhu.timetable.config.store.User
import vip.mystery0.xhu.timetable.config.store.UserStore.withAutoLoginOnce
import vip.mystery0.xhu.timetable.config.store.getConfigStore
import vip.mystery0.xhu.timetable.model.response.ExamResponse
import vip.mystery0.xhu.timetable.utils.between
import vip.mystery0.xhu.timetable.utils.betweenDays
import vip.mystery0.xhu.timetable.utils.now
import vip.mystery0.xhu.timetable.viewmodel.Exam
import vip.mystery0.xhu.timetable.viewmodel.ExamStatus
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Instant

object ExamRepo : BaseDataRepo {
    private val examApi: ExamApi by inject()

    private val globalPagingConfig: PagingConfig
        @AnyThread get() = PagingConfig(
            pageSize = 10,
            initialLoadSize = 10,
        )

    fun getExamListStream(user: User): Flow<PagingData<Exam>> =
        Pager(
            config = globalPagingConfig,
            pagingSourceFactory = {
                buildPageSource { index, size ->
                    checkForceLoadFromCloud(true)

                    val nowYear = getConfigStore { nowYear }
                    val nowTerm = getConfigStore { nowTerm }

                    val page = user.withAutoLoginOnce {
                        examApi.examList(it, nowYear, nowTerm, index, size)
                    }
                    val now = Clock.System.now()
                    withContext(Dispatchers.Default) {
                        page.suspendMap {
                            mapExam(it, now)
                        }.apply {
                            items.sortedWith(object : Comparator<Exam> {
                                override fun compare(a: Exam, b: Exam): Int {
                                    if (a.examStatus == b.examStatus) {
                                        return a.date.compareTo(b.date)
                                    }
                                    return a.examStatus.index.compareTo(b.examStatus.index)
                                }
                            })
                        }
                    }
                }
            }
        ).flow

    private suspend fun mapExam(response: ExamResponse, now: Instant): Exam {
        val examStatus = when {
            now < response.examStartTimeMills -> ExamStatus.BEFORE
            now > response.examEndTimeMills -> ExamStatus.AFTER
            else -> ExamStatus.IN
        }
        val time = buildString {
            append(response.examStartTime.format(Formatter.TIME_NO_SECONDS))
            append(" - ")
            append(response.examEndTime.format(Formatter.TIME_NO_SECONDS))
        }
        val statusShowText = when (examStatus) {
            ExamStatus.BEFORE -> {
                val duration = Duration.between(now, response.examStartTimeMills)
                val remainDays = duration.inWholeDays
                if (remainDays > 0L) {
                    //还有超过一天的时间，那么显示 x天
                    val dayDuration = betweenDays(LocalDate.now(), response.examDay)
                    //如果在明天之外，那么不计算小时
                    if (dayDuration > 1) {
                        "${remainDays + 1}\n天"
                    } else {
                        "${remainDays}\n天"
                    }
                } else {
                    //剩余时间不足一天，显示 x小时
                    val remainHours = duration.inWholeHours
                    "${remainHours}\n小时后"
                }
            }

            ExamStatus.IN -> {
                //考试中
                "今天"
            }

            ExamStatus.AFTER -> {
                //考试后
                "已结束"
            }
        }
        return Exam(
            CourseColorRepo.getCourseColorByName(response.courseName),
            response.examDay,
            response.examDay.format(LocalDate.Formats.ISO),
            response.seatNo,
            response.courseName,
            response.examName,
            response.location,
            time,
            response.examRegion,
            examStatus,
            statusShowText,
        )
    }

    suspend fun getTomorrowExamList(): List<Exam> {
        checkForceLoadFromCloud(true)

        val nowYear = getConfigStore { nowYear }
        val nowTerm = getConfigStore { nowTerm }

        val now = Clock.System.now()
        return withContext(Dispatchers.Default) {
            mainUser().withAutoLoginOnce {
                examApi.tomorrowExamList(it, nowYear, nowTerm)
            }.map {
                mapExam(it, now)
            }
        }
    }
}