package vip.mystery0.xhu.timetable.module

import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import vip.mystery0.xhu.timetable.viewmodel.AccountManagementViewModel
import vip.mystery0.xhu.timetable.viewmodel.BackgroundViewModel
import vip.mystery0.xhu.timetable.viewmodel.ClassSettingsViewModel
import vip.mystery0.xhu.timetable.viewmodel.CourseRoomViewModel
import vip.mystery0.xhu.timetable.viewmodel.CustomCourseColorViewModel
import vip.mystery0.xhu.timetable.viewmodel.CustomCourseViewModel
import vip.mystery0.xhu.timetable.viewmodel.CustomThingViewModel
import vip.mystery0.xhu.timetable.viewmodel.CustomUiViewModel
import vip.mystery0.xhu.timetable.viewmodel.ExamViewModel
import vip.mystery0.xhu.timetable.viewmodel.ExpScoreViewModel
import vip.mystery0.xhu.timetable.viewmodel.FeedbackViewModel
import vip.mystery0.xhu.timetable.viewmodel.JobHistoryViewModel
import vip.mystery0.xhu.timetable.viewmodel.LoginViewModel
import vip.mystery0.xhu.timetable.viewmodel.MainViewModel
import vip.mystery0.xhu.timetable.viewmodel.NoticeViewModel
import vip.mystery0.xhu.timetable.viewmodel.PagerMainViewModel
import vip.mystery0.xhu.timetable.viewmodel.PagerProfileViewModel
import vip.mystery0.xhu.timetable.viewmodel.SchoolCalendarViewModel
import vip.mystery0.xhu.timetable.viewmodel.SchoolTimetableViewModel
import vip.mystery0.xhu.timetable.viewmodel.ScoreViewModel
import vip.mystery0.xhu.timetable.viewmodel.SettingsViewModel
import vip.mystery0.xhu.timetable.viewmodel.SplashImageViewModel
import vip.mystery0.xhu.timetable.viewmodel.StarterViewModel
import vip.mystery0.xhu.timetable.viewmodel.UrgeViewModel

expect fun platformViewModelModule(module: Module)

val viewModelModule = module {
    viewModel { PagerMainViewModel() }
    viewModel { PagerProfileViewModel() }
    viewModel { AccountManagementViewModel() }
    viewModel { BackgroundViewModel() }
    viewModel { ClassSettingsViewModel() }
    viewModel { CourseRoomViewModel() }
    viewModel { CustomCourseColorViewModel() }
    viewModel { CustomCourseViewModel() }
    viewModel { CustomThingViewModel() }
    viewModel { CustomUiViewModel() }
    viewModel { ExamViewModel() }
    viewModel { ExpScoreViewModel() }
    viewModel { FeedbackViewModel() }
    viewModel { JobHistoryViewModel() }
    viewModel { LoginViewModel() }
    viewModel { MainViewModel() }
    viewModel { NoticeViewModel() }
    viewModel { SchoolCalendarViewModel() }
    viewModel { ScoreViewModel() }
    viewModel { SettingsViewModel() }
    viewModel { SplashImageViewModel() }
    viewModel { StarterViewModel() }
    viewModel { UrgeViewModel() }
    viewModel { SchoolTimetableViewModel() }

    platformViewModelModule(this)
}