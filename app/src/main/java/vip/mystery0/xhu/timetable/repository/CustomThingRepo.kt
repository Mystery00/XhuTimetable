package vip.mystery0.xhu.timetable.repository

import vip.mystery0.xhu.timetable.config.User
import vip.mystery0.xhu.timetable.model.CustomThing
import vip.mystery0.xhu.timetable.module.Repo

interface CustomThingRepo : Repo {
    suspend fun getCustomThingList(
        user: User,
        year: String,
        term: Int,
    ): List<CustomThing>

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