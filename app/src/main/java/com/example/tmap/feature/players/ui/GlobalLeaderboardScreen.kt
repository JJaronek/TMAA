package com.example.tmap.feature.players.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.tmap.feature.players.data.remote.GlobalPlayerStat
import com.example.tmap.feature.players.repository.FirestoreRepository
import kotlinx.coroutines.launch

@Composable
fun GlobalLeaderboardScreenRoot(onBackClick: () -> Unit) {
    val firestoreRepository = remember { FirestoreRepository() }

    // Stavy pro uložení dat a zobrazení načítání
    var stats by remember { mutableStateOf<List<GlobalPlayerStat>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    // Spustí se jen jednou při otevření obrazovky a stáhne data z Cloudu
    LaunchedEffect(Unit) {
        stats = firestoreRepository.getGlobalLeaderboard()
        isLoading = false
    }

    GlobalLeaderboardScreen(
        stats = stats,
        isLoading = isLoading,
        onBackClick = onBackClick
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GlobalLeaderboardScreen(
    stats: List<GlobalPlayerStat>,
    isLoading: Boolean,
    onBackClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Světový žebříček 🌍") },
                navigationIcon = {
                    Button(onClick = onBackClick, modifier = Modifier.padding(start = 8.dp)) {
                        Text("Zpět")
                    }
                }
            )
        }
    ) { padding ->
        if (isLoading) {
            // Pokud se data stahují, zobrazíme kolečko uprostřed obrazovky
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            // Data jsou stažená, vykreslíme seznam
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)
            ) {
                item {
                    Row(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                        Text("Hráč", modifier = Modifier.weight(1.5f), fontWeight = FontWeight.Bold)
                        Text("Výhry", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold)
                        Text("Max Průměr", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold)
                    }
                    Divider()
                }

                items(stats) { stat ->
                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)) {
                        Text(stat.playerName, modifier = Modifier.weight(1.5f))
                        Text(stat.wins.toString(), modifier = Modifier.weight(1f))
                        // Zaokrouhlení průměru na 2 desetinná místa
                        Text(String.format("%.2f", stat.bestAverage), modifier = Modifier.weight(1f))
                    }
                    Divider()
                }
            }
        }
    }
}