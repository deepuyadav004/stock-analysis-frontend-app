package com.genxsolutions.growwealth.feature.watchlist

import com.genxsolutions.growwealth.data.local.WatchlistCompanyDao
import com.genxsolutions.growwealth.data.local.WatchlistCompanyEntity
import com.genxsolutions.growwealth.data.local.WatchlistSectorDao
import com.genxsolutions.growwealth.data.local.WatchlistSectorEntity
import com.genxsolutions.growwealth.data.remote.CompanyListItem
import com.genxsolutions.growwealth.data.remote.SectorSignal
import kotlinx.coroutines.flow.Flow

class WatchlistRepository(
    private val sectorDao: WatchlistSectorDao,
    private val companyDao: WatchlistCompanyDao,
) {
    fun observeSectorItems(): Flow<List<WatchlistSectorEntity>> = sectorDao.observeAll()

    fun observeSectorIds(): Flow<List<Int>> = sectorDao.observeSectorIds()

    fun observeCompanyItems(): Flow<List<WatchlistCompanyEntity>> = companyDao.observeAll()

    fun observeCompanyIds(): Flow<List<Int>> = companyDao.observeCompanyIds()

    suspend fun toggle(sector: SectorSignal) {
        if (sectorDao.exists(sector.sectorId)) {
            sectorDao.deleteById(sector.sectorId)
        } else {
            sectorDao.upsert(
                WatchlistSectorEntity(
                    sectorId = sector.sectorId,
                    sectorName = sector.sectorName,
                    signal = sector.signal,
                    confidence = sector.confidence,
                    sentimentScore = sector.sentimentScore,
                    snapshotDate = sector.snapshotDate
                )
            )
        }
    }

    suspend fun toggle(company: CompanyListItem) {
        if (companyDao.exists(company.companyId)) {
            companyDao.deleteById(company.companyId)
        } else {
            companyDao.upsert(
                WatchlistCompanyEntity(
                    companyId = company.companyId,
                    companyName = company.companyName,
                    ticker = company.ticker,
                    exchangeCode = company.exchangeCode,
                    latestClose = company.latestClose,
                    dayChangePct = company.dayChangePct,
                    latestDate = company.latestDate
                )
            )
        }
    }

    suspend fun remove(sectorId: Int) {
        sectorDao.deleteById(sectorId)
    }

    suspend fun removeCompany(companyId: Int) {
        companyDao.deleteById(companyId)
    }
}
