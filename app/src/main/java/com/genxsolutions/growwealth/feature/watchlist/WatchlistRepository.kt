package com.genxsolutions.growwealth.feature.watchlist

import com.genxsolutions.growwealth.data.local.WatchlistSectorDao
import com.genxsolutions.growwealth.data.local.WatchlistSectorEntity
import com.genxsolutions.growwealth.data.remote.SectorSignal
import kotlinx.coroutines.flow.Flow

class WatchlistRepository(private val dao: WatchlistSectorDao) {
    fun observeItems(): Flow<List<WatchlistSectorEntity>> = dao.observeAll()

    fun observeSectorIds(): Flow<List<Int>> = dao.observeSectorIds()

    suspend fun toggle(sector: SectorSignal) {
        if (dao.exists(sector.sectorId)) {
            dao.deleteById(sector.sectorId)
        } else {
            dao.upsert(
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

    suspend fun remove(sectorId: Int) {
        dao.deleteById(sectorId)
    }
}
