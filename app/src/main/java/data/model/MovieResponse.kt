package com.yusufozturk.cinetrack.data.model

data class MovieResponse(
    val page: Int,
    val results: List<Movie>,

    @com.google.gson.annotations.SerializedName("total_pages")
    val totalPages: Int
)