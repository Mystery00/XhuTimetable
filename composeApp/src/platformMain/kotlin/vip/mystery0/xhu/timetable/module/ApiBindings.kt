package vip.mystery0.xhu.timetable.module

import de.jensklingenberg.ktorfit.Ktorfit
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import vip.mystery0.xhu.timetable.api.createAggregationApi
import vip.mystery0.xhu.timetable.api.createBackgroundApi
import vip.mystery0.xhu.timetable.api.createCalendarApi
import vip.mystery0.xhu.timetable.api.createClassroomApi
import vip.mystery0.xhu.timetable.api.createCommonApi
import vip.mystery0.xhu.timetable.api.createCourseApi
import vip.mystery0.xhu.timetable.api.createCustomCourseApi
import vip.mystery0.xhu.timetable.api.createCustomThingApi
import vip.mystery0.xhu.timetable.api.createExamApi
import vip.mystery0.xhu.timetable.api.createFeedbackApi
import vip.mystery0.xhu.timetable.api.createJobApi
import vip.mystery0.xhu.timetable.api.createMenuApi
import vip.mystery0.xhu.timetable.api.createNoticeApi
import vip.mystery0.xhu.timetable.api.createPoemsApi
import vip.mystery0.xhu.timetable.api.createScoreApi
import vip.mystery0.xhu.timetable.api.createUrgeApi
import vip.mystery0.xhu.timetable.api.createUserApi

actual fun Module.registerApiBindings() {
    single { get<Ktorfit>(named(RETROFIT)).createCommonApi() }
    single { get<Ktorfit>(named(RETROFIT)).createMenuApi() }
    single { get<Ktorfit>(named(RETROFIT)).createUserApi() }
    single { get<Ktorfit>(named(RETROFIT)).createAggregationApi() }
    single { get<Ktorfit>(named(RETROFIT)).createCourseApi() }
    single { get<Ktorfit>(named(RETROFIT)).createExamApi() }
    single { get<Ktorfit>(named(RETROFIT)).createScoreApi() }
    single { get<Ktorfit>(named(RETROFIT)).createCustomCourseApi() }
    single { get<Ktorfit>(named(RETROFIT)).createCustomThingApi() }
    single { get<Ktorfit>(named(RETROFIT)).createNoticeApi() }
    single { get<Ktorfit>(named(RETROFIT)).createClassroomApi() }
    single { get<Ktorfit>(named(RETROFIT)).createCalendarApi() }
    single { get<Ktorfit>(named(RETROFIT)).createBackgroundApi() }
    single { get<Ktorfit>(named(RETROFIT)).createUrgeApi() }
    single { get<Ktorfit>(named(RETROFIT)).createJobApi() }
    single { get<Ktorfit>(named(RETROFIT_POEMS)).createPoemsApi() }
    single { get<Ktorfit>(named(RETROFIT_WS)).createFeedbackApi() }
}
