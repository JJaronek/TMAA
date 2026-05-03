package com.example.tmap.feature.game.ui // Dej to do správné složky

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tmap.core.network.GiphyRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WinnerViewModel @Inject constructor(
    private val repository: GiphyRepository
) : ViewModel() {

    // Stav pro uložení odkazu na stažený GIF
    private val _gifUrl = MutableStateFlow<String?>(null)
    val gifUrl = _gifUrl.asStateFlow()

    fun loadWinnerGif() {
        viewModelScope.launch {
            // Zavoláme repozitář, a když se to povede (onSuccess), uložíme URL do stavu
            repository.getWinnerGifUrl().fold(
                onSuccess = { url -> _gifUrl.value = url },
                onFailure = { error ->
                    // Debug ori logcat
                    android.util.Log.e("GIPHY_ERROR", "Chyba stahování: ${error.message}", error)
                }
            )
        }
    }
}