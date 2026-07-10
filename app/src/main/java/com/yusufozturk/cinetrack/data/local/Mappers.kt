package com.yusufozturk.cinetrack.data.local

import com.yusufozturk.cinetrack.data.model.Movie

fun WatchlistEntity.toMovie(): Movie {
    return Movie(
        id = id,
        title = title,
        overview = overview,
        posterPath = posterPath,
        backdropPath = backdropPath,
        releaseDate = releaseDate,
        voteAverage = voteAverage,
        genreIds = genreIds
    )
}

fun Movie.toEntity(): WatchlistEntity {
    return WatchlistEntity(
        id = id,
        title = title,
        overview = overview,
        posterPath = posterPath,
        backdropPath = backdropPath,
        releaseDate = releaseDate,
        voteAverage = voteAverage,
        genreIds = genreIds
    )
}