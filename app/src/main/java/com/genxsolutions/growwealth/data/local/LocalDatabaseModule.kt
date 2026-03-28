package com.genxsolutions.growwealth.data.local

import android.content.Context
import androidx.room.Room

object LocalDatabaseModule {
    @Volatile
    private var instance: AppDatabase? = null

    fun database(context: Context): AppDatabase {
        return instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "grow_wealth.db"
            ).fallbackToDestructiveMigration().build().also { instance = it }
        }
    }
}
