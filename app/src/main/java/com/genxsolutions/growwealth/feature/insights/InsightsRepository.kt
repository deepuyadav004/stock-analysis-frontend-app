package com.genxsolutions.growwealth.feature.insights

import com.genxsolutions.growwealth.data.remote.GrowWealthApi
import com.genxsolutions.growwealth.data.remote.InsightsCompareResponse
import com.genxsolutions.growwealth.data.remote.InsightsStabilityResponse

class InsightsRepository(private val api: GrowWealthApi) {
    suspend fun fetchCompare(days: Int): InsightsCompareResponse =
        api.getInsightsSectorCompare(days = days)

    suspend fun fetchStability(days: Int = 30): InsightsStabilityResponse =
        api.getInsightsSignalStability(days = days)
}
