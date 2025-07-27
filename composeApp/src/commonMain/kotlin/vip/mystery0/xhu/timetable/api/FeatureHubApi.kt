package vip.mystery0.xhu.timetable.api

import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.Query

interface FeatureHubApi {
    @GET("features")
    suspend fun getFeature(@Query("apiKey") apiKey: String)
}