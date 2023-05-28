package vip.mystery0.xhu.timetable.repository

import androidx.annotation.AnyThread
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import org.koin.core.component.inject
import vip.mystery0.xhu.timetable.api.ScoreApi
import vip.mystery0.xhu.timetable.base.BaseDataRepo
import vip.mystery0.xhu.timetable.base.buildPageSource
import vip.mystery0.xhu.timetable.config.store.User
import vip.mystery0.xhu.timetable.config.store.UserStore.withAutoLoginOnce
import vip.mystery0.xhu.timetable.model.response.ExperimentScoreResponse
import vip.mystery0.xhu.timetable.model.response.ScoreGpaResponse
import vip.mystery0.xhu.timetable.model.response.ScoreResponse

object ScoreRepo : BaseDataRepo {
    private val scoreApi: ScoreApi by inject()

    private val globalPagingConfig: PagingConfig
        @AnyThread get() = PagingConfig(
            pageSize = 10,
            initialLoadSize = 10,
        )

    fun getScoreListStream(user: User, year: Int, term: Int): Flow<PagingData<ScoreResponse>> =
        Pager(
            config = globalPagingConfig,
            pagingSourceFactory = {
                buildPageSource { index, size ->
                    checkForceLoadFromCloud(true)

                    user.withAutoLoginOnce {
                        scoreApi.scoreList(it, year, term, index, size)
                    }
                }
            }
        ).flow

    suspend fun fetchExpScoreList(user: User, year: Int, term: Int): List<ExperimentScoreResponse> {
        checkForceLoadFromCloud(true)

        val response = user.withAutoLoginOnce {
            scoreApi.experimentScoreList(it, year, term)
        }
        return response
    }

    suspend fun getGpa(user: User, year: Int, term: Int): ScoreGpaResponse {
        checkForceLoadFromCloud(true)

        val response = user.withAutoLoginOnce {
            scoreApi.gpa(it, year, term)
        }
        return response
    }
}