package vip.mystery0.xhu.timetable.repository.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import vip.mystery0.xhu.timetable.model.entity.CustomThing

@Dao
interface CustomThingDao {
    @Insert
    fun saveCustomThing(customThing: CustomThing)

    @Delete
    fun deleteCustomThing(customThing: CustomThing)

    @Query("SELECT * FROM tb_custom_thing where studentId = :username and year = :year and term = :term")
    suspend fun queryCustomThingList(
        username: String,
        year: String,
        term: Int,
    ): List<CustomThing>
}