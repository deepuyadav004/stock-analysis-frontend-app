package com.genxsolutions.growwealth.data.remote

import retrofit2.http.GET
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
}
