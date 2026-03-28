package com.genxsolutions.growwealth.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface WatchlistSectorDao {
    @Query("SELECT * FROM watchlist_sectors ORDER BY updatedAt DESC")
    fun observeAll(): Flow<List<WatchlistSectorEntity>>

    @Query("SELECT sectorId FROM watchlist_sectors")
    fun observeSectorIds(): Flow<List<Int>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: WatchlistSectorEntity)

    @Query("DELETE FROM watchlist_sectors WHERE sectorId = :sectorId")
    suspend fun deleteById(sectorId: Int)

    @Query("SELECT EXISTS(SELECT 1 FROM watchlist_sectors WHERE sectorId = :sectorId)")
    suspend fun exists(sectorId: Int): Boolean
}
