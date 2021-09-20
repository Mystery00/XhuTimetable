package vip.mystery0.xhu.timetable.module

import org.koin.core.module.Module
import org.koin.dsl.module

fun moduleList(): List<Module> =
    listOf(
        appModule,
    )

private val appModule = module {
//    single { androidContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager }
}