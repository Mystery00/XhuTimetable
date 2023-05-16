package vip.mystery0.xhu.timetable.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.component.inject
import vip.mystery0.xhu.timetable.api.AggregationApi
import vip.mystery0.xhu.timetable.base.BaseDataRepo
import vip.mystery0.xhu.timetable.config.store.UserStore.withAutoLoginOnce
import vip.mystery0.xhu.timetable.config.store.getConfigStore
import vip.mystery0.xhu.timetable.model.TodayCourseView
import vip.mystery0.xhu.timetable.model.TodayThingView
import vip.mystery0.xhu.timetable.model.WeekCourseView
import vip.mystery0.xhu.timetable.model.transfer.AggregationView
import vip.mystery0.xhu.timetable.repository.local.AggregationLocalRepo

object AggregationRepo : BaseDataRepo {
    private val aggregationApi: AggregationApi by inject()

    suspend fun fetchAggregationMainPage(
        forceLoadFromCloud: Boolean,
        showCustomCourse: Boolean,
        showCustomThing: Boolean,
    ): AggregationView {
        checkForceLoadFromCloud(forceLoadFromCloud)

        val nowYear = getConfigStore { nowYear }
        val nowTerm = getConfigStore { nowTerm }
        val userList = requestUserList()

        val todayViewList = ArrayList<TodayCourseView>()
        val weekViewList = ArrayList<WeekCourseView>()
        val todayThingList = ArrayList<TodayThingView>()

        if (isOnline) {
            withContext(Dispatchers.Default) {
                userList.forEach { user ->
                    val response = user.withAutoLoginOnce {
                        aggregationApi.pageMain(
                            it,
                            nowYear,
                            nowTerm,
                            showCustomCourse,
                            showCustomThing
                        )
                    }
                    response.courseList.forEach { course ->
                        todayViewList.add(TodayCourseView.valueOf(course, user))
                        weekViewList.add(WeekCourseView.valueOf(course, user))
                    }
                    response.experimentCourseList.forEach { experimentCourse ->
                        todayViewList.add(TodayCourseView.valueOf(experimentCourse, user))
                        weekViewList.add(WeekCourseView.valueOf(experimentCourse, user))
                    }
                    response.customCourseList.forEach { customCourse ->
                        todayViewList.add(TodayCourseView.valueOf(customCourse, user))
                        weekViewList.add(WeekCourseView.valueOf(customCourse, user))
                    }
                    response.customThingList.forEach { customThing ->
                        todayThingList.add(TodayThingView.valueOf(customThing, user))
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
                    weekViewList.add(WeekCourseView.valueOf(course, user))
                }
                response.experimentCourseList.forEach { experimentCourse ->
                    todayViewList.add(TodayCourseView.valueOf(experimentCourse, user))
                    weekViewList.add(WeekCourseView.valueOf(experimentCourse, user))
                }
                response.customCourseList.forEach { customCourse ->
                    todayViewList.add(TodayCourseView.valueOf(customCourse, user))
                    weekViewList.add(WeekCourseView.valueOf(customCourse, user))
                }
                response.customThingList.forEach { customThing ->
                    todayThingList.add(TodayThingView.valueOf(customThing, user))
                }
            }
        }
        return AggregationView(todayViewList, weekViewList, todayThingList)
    }
}