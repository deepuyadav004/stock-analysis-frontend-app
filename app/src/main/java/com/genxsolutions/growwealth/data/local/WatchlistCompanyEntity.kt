package com.genxsolutions.growwealth.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "watchlist_companies")
data class WatchlistCompanyEntity(
    @PrimaryKey val companyId: Int,
    val companyName: String,
    val ticker: String,
    val exchangeCode: String,
    val latestClose: Double,
    val dayChangePct: Double,
    val latestDate: String?,
    val updatedAt: Long = System.currentTimeMillis()
)
