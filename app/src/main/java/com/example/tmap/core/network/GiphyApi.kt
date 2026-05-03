package com.example.tmap.core.network

import com.example.tmap.BuildConfig // Tady se naimportuje tvůj skrytý klíč z local.properties
import retrofit2.http.GET
import retrofit2.http.Query

interface GiphyApi {
    // Endpoint pro náhodný GIF podle klíčového slova
    @GET("v1/gifs/random")
    suspend fun getRandomWinnerGif(
        @Query("api_key") apiKey: String = BuildConfig.GIPHY_API_KEY,
        @Query("tag") tag: String = "darts winner", // Hledáme šipkařské vítěze
        @Query("rating") rating: String = "g"
    ): GiphyResponse
}