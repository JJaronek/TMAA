package com.example.tmap.core.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.tmap.feature.players.data.local.Match
import com.example.tmap.feature.players.data.local.Player
import com.example.tmap.feature.players.data.local.PlayerDao

@Database(entities = [Player::class, Match::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun playerDao(): PlayerDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "tmap_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}