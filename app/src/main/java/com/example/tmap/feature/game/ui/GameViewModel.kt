package com.example.tmap.feature.game.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.navigation.toRoute
import com.example.tmap.core.navigation.GameRoute
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

// Nová struktura pro uchování jednoho hodu
data class ThrowRecord(
    val playerName: String,
    val score: Int,
    val isBust: Boolean,
    val remainingScore: Int
)

data class GameState(
    val p1Id: Long,
    val p1Name: String,
    val p2Id: Long,
    val p2Name: String,
    val p1Score: Int,
    val p2Score: Int,
    val isDoubleOut: Boolean,
    val isP1Turn: Boolean = true,
    val errorMessage: String? = null,
    val winnerId: Long? = null,
    val winnerName: String? = null,
    val throwHistory: List<ThrowRecord> = emptyList(),
    val p1Turns: Int = 0,
    val p2Turns: Int = 0,
    val p1Average: Double = 0.0,
    val p2Average: Double = 0.0
)

class GameViewModel(savedStateHandle: SavedStateHandle) : ViewModel() {

    private val routeData = savedStateHandle.toRoute<GameRoute>()

    private val _state = MutableStateFlow(
        GameState(
            p1Id = routeData.player1Id,
            p1Name = routeData.player1Name,
            p2Id = routeData.player2Id,
            p2Name = routeData.player2Name,
            p1Score = routeData.startingScore,
            p2Score = routeData.startingScore,
            isDoubleOut = routeData.isDoubleOut
        )
    )
    val state = _state.asStateFlow()

    fun throwDart(score: Int) {
        val currentState = _state.value
        if (currentState.winnerId != null) return

        if (score < 0 || score > 180) {
            _state.value = currentState.copy(errorMessage = "Neplatný hod (musí být 0-180)")
            return
        }

        val currentScore = if (currentState.isP1Turn) currentState.p1Score else currentState.p2Score
        val newScore = currentScore - score

        var isBust = false
        if (newScore < 0 || (currentState.isDoubleOut && newScore == 1)) {
            isBust = true
        }

        val finalScore = if (isBust) currentScore else newScore
        val hasWon = finalScore == 0

        val currentPlayerName = if (currentState.isP1Turn) currentState.p1Name else currentState.p2Name

        // Výpočet průměrů
        val newP1Turns = if (currentState.isP1Turn) currentState.p1Turns + 1 else currentState.p1Turns
        val newP2Turns = if (!currentState.isP1Turn) currentState.p2Turns + 1 else currentState.p2Turns

        val currentP1ScoreForAvg = if (currentState.isP1Turn) finalScore else currentState.p1Score
        val currentP2ScoreForAvg = if (!currentState.isP1Turn) finalScore else currentState.p2Score

        val p1Avg = if (newP1Turns > 0) (routeData.startingScore - currentP1ScoreForAvg).toDouble() / newP1Turns else 0.0
        val p2Avg = if (newP2Turns > 0) (routeData.startingScore - currentP2ScoreForAvg).toDouble() / newP2Turns else 0.0

        val currentThrow = ThrowRecord(
            playerName = currentPlayerName,
            score = score,
            isBust = isBust,
            remainingScore = finalScore
        )
        val updatedHistory = listOf(currentThrow) + currentState.throwHistory

        _state.value = currentState.copy(
            p1Score = currentP1ScoreForAvg,
            p2Score = currentP2ScoreForAvg,
            isP1Turn = if (hasWon) currentState.isP1Turn else !currentState.isP1Turn,
            errorMessage = if (isBust) "Bust! (Přehozeno)" else null,
            winnerId = if (hasWon) (if (currentState.isP1Turn) currentState.p1Id else currentState.p2Id) else null,
            winnerName = if (hasWon) currentPlayerName else null,
            throwHistory = updatedHistory,
            p1Turns = newP1Turns,
            p2Turns = newP2Turns,
            p1Average = p1Avg,
            p2Average = p2Avg
        )
    }

    fun clearError() {
        if (_state.value.errorMessage != null) {
            _state.value = _state.value.copy(errorMessage = null)
        }
    }
}