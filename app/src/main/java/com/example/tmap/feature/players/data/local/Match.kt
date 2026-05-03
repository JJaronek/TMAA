package com.example.tmap.feature.players.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "matches")
data class Match(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val player1Id: Long,
    val player2Id: Long,
    val winnerId: Long,
    val p1Average: Double,
    val p2Average: Double,
    val timestamp: Long = System.currentTimeMillis()
)