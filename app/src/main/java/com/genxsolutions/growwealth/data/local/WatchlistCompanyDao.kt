package com.genxsolutions.growwealth.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface WatchlistCompanyDao {
    @Query("SELECT * FROM watchlist_companies ORDER BY updatedAt DESC")
    fun observeAll(): Flow<List<WatchlistCompanyEntity>>

    @Query("SELECT companyId FROM watchlist_companies")
    fun observeCompanyIds(): Flow<List<Int>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: WatchlistCompanyEntity)

    @Query("DELETE FROM watchlist_companies WHERE companyId = :companyId")
    suspend fun deleteById(companyId: Int)

    @Query("SELECT EXISTS(SELECT 1 FROM watchlist_companies WHERE companyId = :companyId)")
    suspend fun exists(companyId: Int): Boolean
}
