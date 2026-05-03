package com.example.tmap.feature.setup.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tmap.core.database.AppDatabase
import com.example.tmap.feature.players.data.local.Player
import com.example.tmap.feature.players.repository.PlayerRepository

@Composable
fun SetupScreenRoot(
    onNavigateToGame: (Int, Boolean, Long, String, Long, String) -> Unit,
    onNavigateToLeaderboard: () -> Unit,
    onNavigateToGlobalLeaderboard: () -> Unit
) {
    // Inicializace databáze a repozitáře přímo v Compose
    val context = LocalContext.current
    val database = remember { AppDatabase.getDatabase(context) }
    val repository = remember { PlayerRepository(database.playerDao()) }

    // Vytvoření ViewModelu pomocí naší Factory
    val viewModel: SetupViewModel = viewModel(factory = SetupViewModel.Factory(repository))

    val state by viewModel.state.collectAsState()
    val players by viewModel.players.collectAsState()

    SetupScreen(
        state = state,
        players = players,
        onScoreChange = viewModel::setScore,
        onDoubleOutChange = viewModel::toggleDoubleOut,
        onPlayer1Selected = viewModel::selectPlayer1,
        onPlayer2Selected = viewModel::selectPlayer2,
        onAddNewPlayer = viewModel::addNewPlayer,
        onStartGame = {
            if (state.player1 != null && state.player2 != null) {
                onNavigateToGame(
                    state.startingScore,
                    state.isDoubleOut,
                    state.player1!!.id,
                    state.player1!!.name,
                    state.player2!!.id,
                    state.player2!!.name
                )
            }
        },
        onNavigateToLeaderboard = onNavigateToLeaderboard,
        onNavigateToGlobalLeaderboard = onNavigateToGlobalLeaderboard
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetupScreen(
    state: SetupState,
    players: List<Player>,
    onScoreChange: (Int) -> Unit,
    onDoubleOutChange: (Boolean) -> Unit,
    onPlayer1Selected: (Player) -> Unit,
    onPlayer2Selected: (Player) -> Unit,
    onAddNewPlayer: (String) -> Unit,
    onStartGame: () -> Unit,
    onNavigateToLeaderboard: () -> Unit,
    onNavigateToGlobalLeaderboard: () -> Unit
) {
    var newPlayerName by remember { mutableStateOf("") }

    // Stavy pro otevření/zavření rozbalovacích menu
    var expandedP1 by remember { mutableStateOf(false) }
    var expandedP2 by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Nová hra", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(24.dp))

        // --- ZADÁNÍ NOVÉHO HRÁČE ---
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = newPlayerName,
                onValueChange = { newPlayerName = it },
                label = { Text("Jméno nového hráče") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = {
                onAddNewPlayer(newPlayerName)
                newPlayerName = ""
            }) {
                Text("Přidat")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- VÝBĚR HRÁČŮ ---
        ExposedDropdownMenuBox(
            expanded = expandedP1,
            onExpandedChange = { expandedP1 = !expandedP1 }
        ) {
            OutlinedTextField(
                value = state.player1?.name ?: "Vyberte Hráče 1",
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedP1) },
                modifier = Modifier.menuAnchor().fillMaxWidth()
            )
            ExposedDropdownMenu(expanded = expandedP1, onDismissRequest = { expandedP1 = false }) {
                players.forEach { player ->
                    DropdownMenuItem(
                        text = { Text(player.name) },
                        onClick = {
                            onPlayer1Selected(player)
                            expandedP1 = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        ExposedDropdownMenuBox(
            expanded = expandedP2,
            onExpandedChange = { expandedP2 = !expandedP2 }
        ) {
            OutlinedTextField(
                value = state.player2?.name ?: "Vyberte Hráče 2",
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedP2) },
                modifier = Modifier.menuAnchor().fillMaxWidth()
            )
            ExposedDropdownMenu(expanded = expandedP2, onDismissRequest = { expandedP2 = false }) {
                players.forEach { player ->
                    DropdownMenuItem(
                        text = { Text(player.name) },
                        onClick = {
                            onPlayer2Selected(player)
                            expandedP2 = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // --- NASTAVENÍ HRY ---
        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(selected = state.startingScore == 301, onClick = { onScoreChange(301) })
            Text("301")
            Spacer(modifier = Modifier.width(16.dp))
            RadioButton(selected = state.startingScore == 501, onClick = { onScoreChange(501) })
            Text("501")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Switch(checked = state.isDoubleOut, onCheckedChange = onDoubleOutChange)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Double Out")
        }

        Spacer(modifier = Modifier.height(48.dp))

        Button(
            onClick = onStartGame,
            modifier = Modifier.fillMaxWidth(),
            enabled = state.player1 != null && state.player2 != null // Tlačítko funguje jen, když jsou vybráni oba
        ) {
            Text("Spustit hru")
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(
            onClick = onNavigateToLeaderboard,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Otevřít Žebříček")
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedButton(
            onClick = onNavigateToGlobalLeaderboard,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Světový žebříček 🌍")
        }
    }
}