package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.data.local.dao.ProfileDao
import com.example.data.local.dao.SleepSessionDao
import com.example.data.local.entity.ProfileEntity
import com.example.data.local.entity.SleepSessionEntity
import com.example.data.local.entity.SleepTagEntity

@Database(
    entities = [ProfileEntity::class, SleepSessionEntity::class, SleepTagEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class SolaceDatabase : RoomDatabase() {
    abstract fun profileDao(): ProfileDao
    abstract fun sleepSessionDao(): SleepSessionDao

    companion object {
        @Volatile
        private var INSTANCE: SolaceDatabase? = null

        fun getDatabase(context: Context): SolaceDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SolaceDatabase::class.java,
                    "solace_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
