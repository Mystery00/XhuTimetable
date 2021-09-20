package vip.mystery0.xhu.timetable.module

import android.content.ClipboardManager
import android.content.Context
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

fun moduleList(): List<Module> =
    listOf(
        appModule,
        networkModule,
        repoModule,
    )

private val appModule = module {
    single { androidContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager }
}