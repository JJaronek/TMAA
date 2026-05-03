package com.example.tmap.feature.players.repository

import com.example.tmap.feature.players.data.local.Player
import com.example.tmap.feature.players.data.local.PlayerDao
import kotlinx.coroutines.flow.Flow
import com.example.tmap.feature.players.data.local.Match

class PlayerRepository(private val playerDao: PlayerDao) {

    // Automaticky sledovaný seznam všech hráčů
    val allPlayers: Flow<List<Player>> = playerDao.getAllPlayers()

    // Funkce pro přidání nového hráče
    suspend fun addPlayer(name: String) {
        if (name.isNotBlank()) {
            val newPlayer = Player(name = name.trim())
            playerDao.insertPlayer(newPlayer)
        }
    }

    // Automaticky sledovaný seznam všech zápasů
    val allMatches: Flow<List<Match>> = playerDao.getAllMatches()

    // Funkce pro uložení odehraného zápasu
    suspend fun addMatch(match: Match) {
        playerDao.insertMatch(match)
    }

}