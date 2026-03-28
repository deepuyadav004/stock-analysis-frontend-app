package com.genxsolutions.growwealth.feature.sectors

import com.genxsolutions.growwealth.data.remote.GrowWealthApi
import com.genxsolutions.growwealth.data.remote.SectorSignalsResponse

class SectorsRepository(private val api: GrowWealthApi) {
    suspend fun fetchSectorSignals(
        date: String? = null,
        limit: Int = 20,
        offset: Int = 0,
        sort: String = "confidence_desc",
        signal: String? = null,
        confidenceMin: Double? = null,
        confidenceMax: Double? = null
    ): SectorSignalsResponse {
        val signalParam = if (signal.isNullOrBlank()) null else signal
        return api.getSectorSignals(
            date = date,
            limit = limit,
            offset = offset,
            sort = sort,
            signal = signalParam,
            confidenceMin = confidenceMin,
            confidenceMax = confidenceMax
        )
    }
}
