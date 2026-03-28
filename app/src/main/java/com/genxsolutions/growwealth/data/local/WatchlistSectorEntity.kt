package com.genxsolutions.growwealth.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "watchlist_sectors")
data class WatchlistSectorEntity(
    @PrimaryKey val sectorId: Int,
    val sectorName: String,
    val signal: String,
    val confidence: Double,
    val sentimentScore: Double,
    val snapshotDate: String,
    val updatedAt: Long = System.currentTimeMillis()
)
