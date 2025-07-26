package vip.mystery0.xhu.timetable.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import vip.mystery0.xhu.timetable.model.entity.CustomThingEntity

@Dao
interface CustomThingDao {
    @Insert
    suspend fun insert(entity: CustomThingEntity)

    @Delete
    suspend fun delete(entity: CustomThingEntity)

    @Query("select * from tb_custom_thing where studentId = :username")
    suspend fun queryList(
        username: String,
    ): List<CustomThingEntity>
}