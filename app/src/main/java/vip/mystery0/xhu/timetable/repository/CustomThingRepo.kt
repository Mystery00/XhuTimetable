package vip.mystery0.xhu.timetable.repository

import vip.mystery0.xhu.timetable.config.store.User
import vip.mystery0.xhu.timetable.model.CustomThing
import vip.mystery0.xhu.timetable.model.transfer.PageResult
import vip.mystery0.xhu.timetable.module.Repo

interface CustomThingRepo : Repo {
    suspend fun fetchCustomThingList(
        user: User,
        lastId: Long,
        size: Int,
    ): PageResult<CustomThing>

    suspend fun saveCustomThingList(
        year: String,
        term: Int,
        studentId: String,
        list: List<CustomThing>,
    ) {
    }

    suspend fun createCustomThing(
        user: User,
        year: String,
        term: Int,
        customThing: CustomThing,
    ) {
    }

    suspend fun updateCustomThing(
        user: User,
        year: String,
        term: Int,
        customThing: CustomThing,
    ) {
    }

    suspend fun deleteCustomThing(
        user: User,
        year: String,
        term: Int,
        thingId: Long,
    ) {
    }
}