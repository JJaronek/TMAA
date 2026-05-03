package com.example.tmap.feature.setup.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.tmap.feature.players.data.local.Player
import com.example.tmap.feature.players.repository.PlayerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SetupState(
    val startingScore: Int = 501,
    val isDoubleOut: Boolean = true,
    val player1: Player? = null,
    val player2: Player? = null
)

class SetupViewModel(private val repository: PlayerRepository) : ViewModel() {
    private val _state = MutableStateFlow(SetupState())
    val state = _state.asStateFlow()

    // Automaticky se aktualizující seznam hráčů z databáze
    val players: StateFlow<List<Player>> = repository.allPlayers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setScore(score: Int) { _state.value = _state.value.copy(startingScore = score) }
    fun toggleDoubleOut(enabled: Boolean) { _state.value = _state.value.copy(isDoubleOut = enabled) }
    fun selectPlayer1(player: Player) { _state.value = _state.value.copy(player1 = player) }
    fun selectPlayer2(player: Player) { _state.value = _state.value.copy(player2 = player) }

    fun addNewPlayer(name: String) {
        viewModelScope.launch {
            repository.addPlayer(name)
        }
    }

    // Továrna pro vytvoření ViewModelu
    class Factory(private val repository: PlayerRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SetupViewModel(repository) as T
        }
    }
}