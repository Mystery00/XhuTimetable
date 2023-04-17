package vip.mystery0.xhu.timetable.repository

import org.koin.core.component.inject
import vip.mystery0.xhu.timetable.api.ScoreApi
import vip.mystery0.xhu.timetable.base.BaseDataRepo
import vip.mystery0.xhu.timetable.config.store.User
import vip.mystery0.xhu.timetable.config.store.UserStore.withAutoLoginOnce
import vip.mystery0.xhu.timetable.model.response.ExperimentScoreResponse
import vip.mystery0.xhu.timetable.model.response.ScoreResponse
import vip.mystery0.xhu.timetable.model.transfer.PageResult

object ScoreRepo : BaseDataRepo {
    private val scoreApi: ScoreApi by inject()

    suspend fun fetchScoreList(user: User, year: Int, term: Int): PageResult<ScoreResponse> {
        checkForceLoadFromCloud(true)

        val response = user.withAutoLoginOnce {
            scoreApi.scoreList(it, year, term)
        }
        return response
    }

    suspend fun fetchExpScoreList(user: User, year: Int, term: Int): PageResult<ExperimentScoreResponse> {
        checkForceLoadFromCloud(true)

        val response = user.withAutoLoginOnce {
            scoreApi.experimentScoreList(it, year, term)
        }
        return response
    }
}