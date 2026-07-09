package com.yusufozturk.cinetrack.data.model

import com.google.gson.annotations.SerializedName

data class Movie(
    val id: Int,
    val title: String,
    val overview: String,

    @SerializedName("poster_path")
    val posterPath: String?,

    @SerializedName("backdrop_path")
    val backdropPath: String?,

    @SerializedName("release_date")
    val releaseDate: String?,

    @SerializedName("vote_average")
    val voteAverage: Double,

    @SerializedName("genre_ids")
    val genreIds: List<Int> = emptyList(),

    // Sadece "rated movies" endpoint'inden gelen cevapta doluyor (kullanıcının TMDB'ye verdiği puan, 0.5-10 arası)
    val rating: Double? = null
)