package com.genxsolutions.growwealth.feature.home

import com.genxsolutions.growwealth.data.remote.GrowWealthApi
import com.genxsolutions.growwealth.data.remote.HomeSummaryResponse
import com.genxsolutions.growwealth.data.remote.SectorSignalsResponse
import com.genxsolutions.growwealth.data.remote.SnapshotLatestResponse

class HomeRepository(private val api: GrowWealthApi) {
    suspend fun fetchSnapshotLatest(): SnapshotLatestResponse = api.getSnapshotLatest()

    suspend fun fetchHomeSummary(date: String?): HomeSummaryResponse = api.getHomeSummary(date)

    suspend fun fetchSectorSignals(date: String?): SectorSignalsResponse =
        api.getSectorSignals(date = date, limit = 20, offset = 0, sort = "confidence_desc")
}
