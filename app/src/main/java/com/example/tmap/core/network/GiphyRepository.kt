package com.example.tmap.core.network

import javax.inject.Inject

class GiphyRepository @Inject constructor(
    private val api: GiphyApi
) {
    suspend fun getWinnerGifUrl(): Result<String> {
        return try {
            val response = api.getRandomWinnerGif()
            // Prokoušeme se naší "matrjoškou" až k samotnému URL odkazu
            val gifUrl = response.data.images.original.url
            Result.success(gifUrl)
        } catch (e: Exception) {
            // Pokud např. vypadne internet, chytíme chybu
            Result.failure(e)
        }
    }
}