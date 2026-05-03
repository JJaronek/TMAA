package com.example.tmap.feature.players.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.tmap.core.database.AppDatabase
import com.example.tmap.feature.players.repository.PlayerRepository
import kotlin.math.round
import com.example.tmap.feature.players.data.local.Match

// Pomocná třída jen pro zobrazení
data class PlayerStat(val name: String, val wins: Int, val bestAverage: Double)

@Composable
fun LeaderboardScreenRoot(onBackClick: () -> Unit) {
    val context = LocalContext.current
    val database = remember { AppDatabase.getDatabase(context) }
    val repository = remember { PlayerRepository(database.playerDao()) }

    val players by repository.allPlayers.collectAsState(initial = emptyList())
    val matches by repository.allMatches.collectAsState(initial = emptyList())

    // Výpočet statistik (seřadíme od nejvíce výher)
    val stats = remember(players, matches) {
        players.map { player ->
            val playerMatches = matches.filter { it.player1Id == player.id || it.player2Id == player.id }
            val wins = matches.count { it.winnerId == player.id }
            val bestAvg = playerMatches.map {
                if (it.player1Id == player.id) it.p1Average else it.p2Average
            }.maxOrNull() ?: 0.0

            PlayerStat(player.name, wins, round(bestAvg * 100) / 100)
        }.sortedByDescending { it.wins }
    }

    LeaderboardScreen(stats = stats, onBackClick = onBackClick)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaderboardScreen(stats: List<PlayerStat>, onBackClick: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Žebříček") },
                navigationIcon = {
                    Button(onClick = onBackClick, modifier = Modifier.padding(start = 8.dp)) {
                        Text("Zpět")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)
        ) {
            item {
                Row(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                    Text("Hráč", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold)
                    Text("Výhry", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold)
                    Text("Max Průměr", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold)
                }
                Divider()
            }

            items(stats) { stat ->
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)) {
                    Text(stat.name, modifier = Modifier.weight(1f))
                    Text(stat.wins.toString(), modifier = Modifier.weight(1f))
                    Text(stat.bestAverage.toString(), modifier = Modifier.weight(1f))
                }
                Divider()
            }
        }
    }
}