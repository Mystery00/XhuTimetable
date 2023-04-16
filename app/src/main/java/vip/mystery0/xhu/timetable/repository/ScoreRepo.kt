package vip.mystery0.xhu.timetable.repository

import org.koin.core.component.inject
import vip.mystery0.xhu.timetable.api.ScoreApi
import vip.mystery0.xhu.timetable.base.BaseDataRepo
import vip.mystery0.xhu.timetable.config.store.User
import vip.mystery0.xhu.timetable.config.store.UserStore.withAutoLoginOnce
import vip.mystery0.xhu.timetable.config.store.getConfigStore
import vip.mystery0.xhu.timetable.model.response.ExperimentScoreResponse
import vip.mystery0.xhu.timetable.model.response.ScoreResponse

object ScoreRepo : BaseDataRepo {
    private val scoreApi: ScoreApi by inject()

    suspend fun fetchScoreList(user: User): List<ScoreResponse> {
        checkForceLoadFromCloud(true)

        val nowYear = getConfigStore { nowYear }
        val nowTerm = getConfigStore { nowTerm }
        val response = user.withAutoLoginOnce {
            scoreApi.scoreList(it, nowYear, nowTerm)
        }
        return response
    }

    suspend fun fetchExpScoreList(user: User): List<ExperimentScoreResponse> {
        checkForceLoadFromCloud(true)

        val nowYear = getConfigStore { nowYear }
        val nowTerm = getConfigStore { nowTerm }
        val response = user.withAutoLoginOnce {
            scoreApi.experimentScoreList(it, nowYear, nowTerm)
        }
        return response
    }
}