package com.genxsolutions.growwealth.feature.companies

import com.genxsolutions.growwealth.data.remote.CompanyListResponse
import com.genxsolutions.growwealth.data.remote.CompanyPerformanceResponse
import com.genxsolutions.growwealth.data.remote.CompanySummaryResponse
import com.genxsolutions.growwealth.data.remote.GrowWealthApi

class CompaniesRepository(private val api: GrowWealthApi) {
    suspend fun fetchCompanies(
        query: String?,
        signal: String?,
        limit: Int,
        offset: Int,
        sort: String
    ): CompanyListResponse = api.getCompaniesList(query, signal, limit, offset, sort)

    suspend fun fetchSummary(companyId: Int): CompanySummaryResponse =
        api.getCompanySummary(companyId)

    suspend fun fetchPerformance(companyId: Int, range: String): CompanyPerformanceResponse =
        api.getCompanyPerformance(companyId, range)
}
