package vip.mystery0.xhu.timetable.module

import org.koin.core.module.Module

fun moduleList(): List<Module> =
    listOf(
        platformModule(),
        databaseModule,
        networkModule,
        viewModelModule,
        repositoryModule,
    )