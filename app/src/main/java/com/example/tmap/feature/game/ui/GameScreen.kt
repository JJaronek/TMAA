package com.example.tmap.feature.game.ui

import android.os.Build
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.platform.LocalContext
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import com.example.tmap.core.database.AppDatabase
import com.example.tmap.feature.players.repository.PlayerRepository
import com.example.tmap.feature.players.data.local.Match
import com.example.tmap.feature.players.repository.FirestoreRepository
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun GameScreenRoot(viewModel: GameViewModel = viewModel()) {
    val state by viewModel.state.collectAsState()

    val context = LocalContext.current
    val database = remember { AppDatabase.getDatabase(context) }
    val localRepository = remember { PlayerRepository(database.playerDao()) }
    val firestoreRepository = remember { FirestoreRepository() }

    LaunchedEffect(state.winnerId) {
        if (state.winnerId != null) {
            // 1. Lokální uložení
            localRepository.addMatch(
                Match(
                    player1Id = state.p1Id, player2Id = state.p2Id,
                    winnerId = state.winnerId!!,
                    p1Average = state.p1Average, p2Average = state.p2Average
                )
            )

            // 2. Odeslání do Cloudu
            val winnerAverage = if (state.winnerId == state.p1Id) state.p1Average else state.p2Average
            firestoreRepository.uploadPlayerStat(
                playerName = state.winnerName!!,
                newAverage = winnerAverage
            )
        }
    }

    GameScreen(
        state = state,
        onThrow = viewModel::throwDart,
        onClearError = viewModel::clearError
    )
}

@Composable
fun GameScreen(
    state: GameState,
    onThrow: (Int) -> Unit,
    onClearError: () -> Unit
) {
    var inputText by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Double Out: ${if (state.isDoubleOut) "ZAPNUTO" else "VYPNUTO"}")

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Hráč 1", style = MaterialTheme.typography.titleLarge)
                Text(
                    text = state.p1Score.toString(),
                    style = MaterialTheme.typography.displayLarge,
                    color = if (state.isP1Turn && state.winnerName == null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Hráč 2", style = MaterialTheme.typography.titleLarge)
                Text(
                    text = state.p2Score.toString(),
                    style = MaterialTheme.typography.displayLarge,
                    color = if (!state.isP1Turn && state.winnerName == null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        //zobrazení výhry
        if (state.winnerName != null) {
            var showWinnerDialog by remember { mutableStateOf(true) }

            Text(
                text = "Vítěz: ${state.winnerName}!",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary
            )

            if (showWinnerDialog) {
                WinnerGifDialog(
                    winnerName = state.winnerName, //reálné jméno
                    onDismiss = {// Zruší dialog po kliknutí na tlačítko
                        showWinnerDialog = false
                    }
                )
            }
        } else {
            if (state.errorMessage != null) {
                Text(
                    text = state.errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            OutlinedTextField(
                value = inputText,
                onValueChange = {
                    inputText = it
                    onClearError()
                },
                label = { Text("Zadejte skóre (0-180)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(0.6f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    val score = inputText.toIntOrNull()
                    if (score != null) {
                        onThrow(score)
                        inputText = ""
                    }
                },
                modifier = Modifier.fillMaxWidth(0.6f)
            ) {
                Text("Zapsat hod")
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // --- HISTORIE HODŮ ---
        Text(
            text = "Historie hodů",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier.fillMaxWidth().weight(1f) // Zabere zbytek obrazovky
        ) {
            items(state.throwHistory) { record ->
                val throwText = if (record.isBust) {
                    "${record.playerName} hodil ${record.score} (Bust!) ➔ Zbývá ${record.remainingScore}"
                } else {
                    "${record.playerName} hodil ${record.score} ➔ Zbývá ${record.remainingScore}"
                }

                Text(
                    text = throwText,
                    modifier = Modifier.padding(vertical = 8.dp),
                    color = if (record.isBust) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                )
                Divider() // Tenká čára mezi záznamy
            }
        }
    }
}

@Composable
fun WinnerGifDialog(
    winnerName: String,
    onDismiss: () -> Unit,
    // Hilt nám sem automaticky dodá ViewModel
    viewModel: WinnerViewModel = hiltViewModel()
) {
    // Sledujeme Flow stav z ViewModelu, abychom věděli, kdy je URL připravené
    val gifUrl by viewModel.gifUrl.collectAsState()
    val context = LocalContext.current

    // Příprava knihovny Coil pro přehrávání animovaných GIFů (jinak by to byl jen statický obrázek)
    val imageLoader = remember {
        ImageLoader.Builder(context)
            .components {
                if (Build.VERSION.SDK_INT >= 28) {
                    add(ImageDecoderDecoder.Factory())
                } else {
                    add(GifDecoder.Factory())
                }
            }
            .build()
    }

    // Spustí se jen jednou při otevření okna a zavolá stahování přes Retrofit
    LaunchedEffect(Unit) {
        viewModel.loadWinnerGif()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("🎯 Máme vítěze!") },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Gratulujeme, $winnerName vyhrává hru!", modifier = Modifier.padding(bottom = 16.dp))

                if (gifUrl != null) {
                    // Když máme URL, vykreslíme stažený GIF
                    AsyncImage(
                        model = gifUrl,
                        contentDescription = "Vítězný GIF z Giphy",
                        imageLoader = imageLoader,
                        modifier = Modifier.size(200.dp)
                    )
                } else {
                    // Dokud Retrofit stahuje data ze sítě, točí se kolečko
                    CircularProgressIndicator(modifier = Modifier.padding(16.dp))
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Zavřít a uložit hru")
            }
        }
    )
}
