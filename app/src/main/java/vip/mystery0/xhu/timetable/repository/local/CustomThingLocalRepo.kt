package vip.mystery0.xhu.timetable.repository.local

import org.koin.core.component.inject
import vip.mystery0.xhu.timetable.config.User
import vip.mystery0.xhu.timetable.config.runOnIo
import vip.mystery0.xhu.timetable.model.CustomThing
import vip.mystery0.xhu.timetable.repository.CustomThingRepo
import vip.mystery0.xhu.timetable.repository.db.dao.CustomThingDao

class CustomThingLocalRepo : CustomThingRepo {
    private val customThingDao: CustomThingDao by inject()

    override suspend fun getCustomThingList(
        user: User,
        year: String,
        term: Int
    ): List<CustomThing> = runOnIo {
        customThingDao.queryCustomThingList(user.studentId, year, term)
            .map { it.toModel() }
    }

    override suspend fun saveCustomThingList(
        year: String,
        term: Int,
        studentId: String,
        list: List<CustomThing>
    ) = runOnIo {
        //删除旧数据
        customThingDao.queryCustomThingList(studentId, year, term).forEach {
            customThingDao.deleteCustomThing(it)
        }
        list.forEach { customThing ->
            customThingDao.saveCustomThing(customThing.toEntity(studentId, year, term))
        }
    }
}