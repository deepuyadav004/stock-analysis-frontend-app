package com.genxsolutions.growwealth.feature.home

import com.genxsolutions.growwealth.data.remote.GrowWealthApi
import com.genxsolutions.growwealth.data.remote.HomeSummaryResponse
import com.genxsolutions.growwealth.data.remote.SectorDetailResponse
import com.genxsolutions.growwealth.data.remote.SectorSignalsResponse
import com.genxsolutions.growwealth.data.remote.SectorTrendsResponse
import com.genxsolutions.growwealth.data.remote.SnapshotLatestResponse

class HomeRepository(private val api: GrowWealthApi) {
    suspend fun fetchSnapshotLatest(): SnapshotLatestResponse = api.getSnapshotLatest()

    suspend fun fetchHomeSummary(date: String?): HomeSummaryResponse = api.getHomeSummary(date)

    suspend fun fetchSectorSignals(date: String?): SectorSignalsResponse =
        api.getSectorSignals(date = date, limit = 20, offset = 0, sort = "confidence_desc")

    suspend fun fetchSectorTrends(date: String?, days: Int, sectorIds: List<Int>): SectorTrendsResponse {
        val idsParam = if (sectorIds.isEmpty()) null else sectorIds.joinToString(",")
        return api.getSectorTrends(date = date, days = days, sectorIds = idsParam)
    }

    suspend fun fetchSectorDetail(sectorId: Int, from: String?, to: String?): SectorDetailResponse =
        api.getSectorDetail(sectorId = sectorId, from = from, to = to)
}
