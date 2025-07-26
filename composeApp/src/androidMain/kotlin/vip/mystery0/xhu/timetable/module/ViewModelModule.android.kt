package vip.mystery0.xhu.timetable.module

import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import vip.mystery0.xhu.timetable.viewmodel.CheckUpdateVewModel
import vip.mystery0.xhu.timetable.viewmodel.PlatformSettingsViewModel

actual fun platformViewModelModule(module: Module) {
    module.viewModel { CheckUpdateVewModel() }
    module.viewModel { PlatformSettingsViewModel() }
}