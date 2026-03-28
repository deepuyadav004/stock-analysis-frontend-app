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
        @Query("sort") sort: String = "confidence_desc",
        @Query("signal") signal: String? = null,
        @Query("confidence_min") confidenceMin: Double? = null,
        @Query("confidence_max") confidenceMax: Double? = null
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

    @GET("v1/insights/sector-compare")
    suspend fun getInsightsSectorCompare(
        @Query("date") date: String? = null,
        @Query("days") days: Int
    ): InsightsCompareResponse

    @GET("v1/insights/signal-stability")
    suspend fun getInsightsSignalStability(
        @Query("date") date: String? = null,
        @Query("days") days: Int = 30
    ): InsightsStabilityResponse

    @GET("v1/companies/list")
    suspend fun getCompaniesList(
        @Query("query") query: String? = null,
        @Query("signal") signal: String? = null,
        @Query("limit") limit: Int = 20,
        @Query("offset") offset: Int = 0,
        @Query("sort") sort: String = "name_asc"
    ): CompanyListResponse

    @GET("v1/companies/{companyId}/summary")
    suspend fun getCompanySummary(
        @Path("companyId") companyId: Int
    ): CompanySummaryResponse

    @GET("v1/companies/{companyId}/performance")
    suspend fun getCompanyPerformance(
        @Path("companyId") companyId: Int,
        @Query("range") range: String
    ): CompanyPerformanceResponse
}
