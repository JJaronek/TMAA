package com.example.tmap.core.network

data class GiphyResponse(
    val data: GiphyData
)

data class GiphyData(
    val images: GiphyImages
)

data class GiphyImages(
    val original: GiphyImage
)

data class GiphyImage(
    val url: String
)