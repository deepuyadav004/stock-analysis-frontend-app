package com.genxsolutions.growwealth.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [WatchlistSectorEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun watchlistSectorDao(): WatchlistSectorDao
}
