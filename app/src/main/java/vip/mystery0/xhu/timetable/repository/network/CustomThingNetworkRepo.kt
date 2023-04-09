package vip.mystery0.xhu.timetable.repository.network

import vip.mystery0.xhu.timetable.config.store.User
import vip.mystery0.xhu.timetable.model.CustomThing
import vip.mystery0.xhu.timetable.module.NetworkRepo
import vip.mystery0.xhu.timetable.module.localRepo
import vip.mystery0.xhu.timetable.module.remoteRepo
import vip.mystery0.xhu.timetable.repository.CustomThingRepo

object CustomThingNetworkRepo : CustomThingRepo, NetworkRepo<CustomThingRepo> {
    override val local: CustomThingRepo by localRepo()
    override val remote: CustomThingRepo by remoteRepo()

    override suspend fun getCustomThingList(
        user: User,
        year: String,
        term: Int
    ): List<CustomThing> =
        dispatch().getCustomThingList(user, year, term)
}