package com.genxsolutions.growwealth.data.remote

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface GrowWealthApi {
    @GET("v1/snapshot/latest")
    suspend fun getSnapshotLatest(): SnapshotLatestResponse

    @GET("v1/home/summary")
    suspend fun getHomeSummary(@Query("date") date: String? = null): HomeSummaryResponse

    @GET("v1/sectors/signals")
    suspend fun getSectorSignals(
        @Query("date") date: String? = null,
        @Query("limit") limit: Int = 20,
        @Query("offset") offset: Int = 0,
        @Query("sort") sort: String = "confidence_desc"
    ): SectorSignalsResponse

    @GET("v1/sectors/trends")
    suspend fun getSectorTrends(
        @Query("date") date: String? = null,
        @Query("days") days: Int = 7,
        @Query("sector_ids") sectorIds: String? = null
    ): SectorTrendsResponse

    @GET("v1/sectors/{sectorId}/detail")
    suspend fun getSectorDetail(
        @Path("sectorId") sectorId: Int,
        @Query("from") from: String? = null,
        @Query("to") to: String? = null
    ): SectorDetailResponse
}
