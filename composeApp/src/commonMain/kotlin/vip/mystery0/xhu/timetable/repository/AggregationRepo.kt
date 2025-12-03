package vip.mystery0.xhu.timetable.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDate
import kotlinx.datetime.format
import kotlinx.datetime.format.char
import org.koin.core.component.inject
import vip.mystery0.xhu.timetable.api.AggregationApi
import vip.mystery0.xhu.timetable.base.BaseDataRepo
import vip.mystery0.xhu.timetable.config.HINT_NETWORK
import vip.mystery0.xhu.timetable.config.store.UserStore.withAutoLoginOnce
import vip.mystery0.xhu.timetable.config.store.getConfigStore
import vip.mystery0.xhu.timetable.model.PracticalCourseView
import vip.mystery0.xhu.timetable.model.TodayCourseView
import vip.mystery0.xhu.timetable.model.TodayThingView
import vip.mystery0.xhu.timetable.model.WeekCourseView
import vip.mystery0.xhu.timetable.model.response.CalendarWeekResponse
import vip.mystery0.xhu.timetable.model.transfer.AggregationView
import vip.mystery0.xhu.timetable.repository.local.AggregationLocalRepo

object AggregationRepo : BaseDataRepo {
    private val dateFormatter = LocalDate.Format {
        year()
        char('-')
        monthNumber()
        char('-')
        day()
    }

    private val aggregationApi: AggregationApi by inject()

    suspend fun fetchAggregationMainPage(
        forceLoadFromCloud: Boolean,
        forceLoadFromLocal: Boolean,
        showCustomCourse: Boolean,
        showCustomThing: Boolean,
    ): AggregationView {
        checkForceLoadFromCloud(forceLoadFromCloud)

        val nowYear = getConfigStore { nowYear }
        val nowTerm = getConfigStore { nowTerm }
        val customAccountTitle = getConfigStore { customAccountTitle }
        val userList = requestUserList()

        val todayViewList = ArrayList<TodayCourseView>()
        val weekViewList = ArrayList<WeekCourseView>()
        val todayThingList = ArrayList<TodayThingView>()
        val practicalCourseList = ArrayList<PracticalCourseView>()

        val (loadFromCloud, loadWarning) = checkLoadFromCloud(
            forceLoadFromCloud,
            forceLoadFromLocal,
        )
        if (loadFromCloud) {
            withContext(Dispatchers.Default) {
                userList.forEach { user ->
                    val response = user.withAutoLoginOnce {
                        aggregationApi.pageMain(
                            it,
                            nowYear,
                            nowTerm,
                            showCustomCourse,
                            showCustomThing,
                        )
                    }
                    response.courseList.forEach { course ->
                        todayViewList.add(TodayCourseView.valueOf(course, user))
                        weekViewList.add(
                            WeekCourseView.valueOf(
                                course,
                                customAccountTitle.formatWeek(user.info)
                            )
                        )
                    }
                    response.experimentCourseList.forEach { experimentCourse ->
                        todayViewList.add(TodayCourseView.valueOf(experimentCourse, user))
                        weekViewList.add(
                            WeekCourseView.valueOf(
                                experimentCourse,
                                customAccountTitle.formatWeek(user.info)
                            )
                        )
                    }
                    response.customCourseList.forEach { customCourse ->
                        todayViewList.add(TodayCourseView.valueOf(customCourse, user))
                        weekViewList.add(
                            WeekCourseView.valueOf(
                                customCourse,
                                customAccountTitle.formatWeek(user.info)
                            )
                        )
                    }
                    response.customThingList.forEach { customThing ->
                        todayThingList.add(TodayThingView.valueOf(customThing, user))
                    }
                    response.practicalCourseList.forEach { course ->
                        practicalCourseList.add(PracticalCourseView.valueOf(course, user))
                    }
                    //保存数据到数据库
                    AggregationLocalRepo.saveResponse(
                        nowYear,
                        nowTerm,
                        user,
                        response,
                        showCustomCourse,
                        showCustomThing,
                    )
                }
            }
        } else {
            userList.forEach { user ->
                val response = AggregationLocalRepo.fetchAggregationMainPage(
                    nowYear,
                    nowTerm,
                    user,
                    showCustomCourse,
                    showCustomThing,
                )
                response.courseList.forEach { course ->
                    todayViewList.add(TodayCourseView.valueOf(course, user))
                    weekViewList.add(
                        WeekCourseView.valueOf(
                            course,
                            customAccountTitle.formatWeek(user.info)
                        )
                    )
                }
                response.experimentCourseList.forEach { experimentCourse ->
                    todayViewList.add(TodayCourseView.valueOf(experimentCourse, user))
                    weekViewList.add(
                        WeekCourseView.valueOf(
                            experimentCourse,
                            customAccountTitle.formatWeek(user.info)
                        )
                    )
                }
                response.customCourseList.forEach { customCourse ->
                    todayViewList.add(TodayCourseView.valueOf(customCourse, user))
                    weekViewList.add(
                        WeekCourseView.valueOf(
                            customCourse,
                            customAccountTitle.formatWeek(user.info)
                        )
                    )
                }
                response.customThingList.forEach { customThing ->
                    todayThingList.add(TodayThingView.valueOf(customThing, user))
                }
                response.practicalCourseList.forEach { course ->
                    practicalCourseList.add(PracticalCourseView.valueOf(course, user))
                }
            }
        }
        return AggregationView(
            todayViewList,
            weekViewList,
            todayThingList,
            practicalCourseList,
            loadWarning,
        )
    }

    suspend fun fetchAggregationCalendarPage(
        forceLoadFromCloud: Boolean,
        forceLoadFromLocal: Boolean,
    ): List<CalendarWeekResponse> {
        val multiAccountMode = getConfigStore { multiAccountMode }
        if (multiAccountMode) {
            //多账号模式下不支持日历
            return emptyList()
        }
        val enableCalendarView = getConfigStore { enableCalendarView }
        if (!enableCalendarView) {
            //未启用日历视图
            return emptyList()
        }
        val (loadFromCloud, _) = checkLoadFromCloud(
            forceLoadFromCloud,
            forceLoadFromLocal,
        )
        val nowYear = getConfigStore { nowYear }
        val nowTerm = getConfigStore { nowTerm }
        val termStartDate = getConfigStore { termStartDate }

        if (loadFromCloud) {
            return mainUser().withAutoLoginOnce {
                aggregationApi.pageCalendar(
                    it,
                    termStartDate.format(dateFormatter),
                    nowYear,
                    nowTerm,
                )
            }
        } else {
            return AggregationLocalRepo.fetchAggregationCalendarPage(
                nowYear,
                nowTerm,
                mainUser(),
                termStartDate,
            )
        }
    }

    private fun checkLoadFromCloud(
        forceLoadFromCloud: Boolean,
        forceLoadFromLocal: Boolean,
    ): Pair<Boolean, String> {
        var loadFromCloud = when {
            forceLoadFromCloud -> true
            forceLoadFromLocal -> false
            else -> isOnline
        }
        var loadWarning = ""
        if (loadFromCloud && !isOnline) {
            //需要从网络加载但是没有网络，降级为从本地加载，并显示一个错误
            loadFromCloud = false
            loadWarning = HINT_NETWORK
        }
        return Pair(loadFromCloud, loadWarning)
    }
}