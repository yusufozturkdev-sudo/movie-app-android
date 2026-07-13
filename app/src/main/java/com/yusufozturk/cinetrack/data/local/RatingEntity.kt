package com.yusufozturk.cinetrack.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "movie_ratings")
data class RatingEntity(
    @PrimaryKey val movieId: Int,
    val rating: Int
)
