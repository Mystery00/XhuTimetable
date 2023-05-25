package vip.mystery0.xhu.timetable.repository

import androidx.annotation.AnyThread
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import org.koin.core.component.inject
import vip.mystery0.xhu.timetable.api.ExamApi
import vip.mystery0.xhu.timetable.base.BaseDataRepo
import vip.mystery0.xhu.timetable.base.buildPageSource
import vip.mystery0.xhu.timetable.config.store.Formatter
import vip.mystery0.xhu.timetable.config.store.User
import vip.mystery0.xhu.timetable.config.store.UserStore.withAutoLoginOnce
import vip.mystery0.xhu.timetable.config.store.getConfigStore
import vip.mystery0.xhu.timetable.model.response.ExamResponse
import vip.mystery0.xhu.timetable.module.betweenDays
import vip.mystery0.xhu.timetable.viewmodel.Exam
import vip.mystery0.xhu.timetable.viewmodel.ExamStatus
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.format.DateTimeFormatter

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
                    val now = Instant.now()
                    withContext(Dispatchers.Default) {
                        page.suspendMap {
                            mapExam(it, now)
                        }.apply {
                            items.sortedWith(object : Comparator<Exam> {
                                override fun compare(o1: Exam, o2: Exam): Int {
                                    if (o1.examStatus == o2.examStatus) {
                                        return o1.date.compareTo(o2.date)
                                    }
                                    return o1.examStatus.index.compareTo(o2.examStatus.index)
                                }
                            })
                        }
                    }
                }
            }
        ).flow

    private suspend fun mapExam(response: ExamResponse, now: Instant): Exam {
        val examStatus = when {
            now.isBefore(response.examStartTimeMills) -> ExamStatus.BEFORE
            now.isAfter(response.examEndTimeMills) -> ExamStatus.AFTER
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
                val remainDays = duration.toDays()
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
                    val remainHours = duration.toHours()
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
            response.examDay.format(DateTimeFormatter.ISO_LOCAL_DATE),
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
}