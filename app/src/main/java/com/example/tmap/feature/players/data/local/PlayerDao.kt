package com.example.tmap.feature.players.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PlayerDao {
    @Insert
    suspend fun insertPlayer(player: Player): Long

    @Insert
    suspend fun insertMatch(match: Match)

    @Query("SELECT * FROM players ORDER BY name ASC")
    fun getAllPlayers(): Flow<List<Player>>

    @Query("SELECT COUNT(*) FROM matches WHERE winnerId = :playerId")
    fun getWinCountForPlayer(playerId: Long): Flow<Int>

    @Query("SELECT * FROM matches ORDER BY timestamp DESC")
    fun getAllMatches(): Flow<List<Match>>

}