package com.example.tmap.core.navigation

import kotlinx.serialization.Serializable

@Serializable
object SetupRoute

@Serializable
data class GameRoute(
    val startingScore: Int,
    val isDoubleOut: Boolean,
    val player1Id: Long,
    val player1Name: String,
    val player2Id: Long,
    val player2Name: String
)

@Serializable
object LeaderboardRoute

@Serializable
object GlobalLeaderboardRoute