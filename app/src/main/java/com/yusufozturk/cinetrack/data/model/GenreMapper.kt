package com.yusufozturk.cinetrack.data.model

object GenreMapper {
    private val genreMap = mapOf(
        28 to "Action",
        12 to "Adventure",
        16 to "Animation",
        35 to "Comedy",
        80 to "Crime",
        99 to "Documentary",
        18 to "Drama",
        10751 to "Family",
        14 to "Fantasy",
        36 to "History",
        27 to "Horror",
        10402 to "Music",
        9648 to "Mystery",
        10749 to "Romance",
        878 to "Sci-Fi",
        10770 to "TV Movie",
        53 to "Thriller",
        10752 to "War",
        37 to "Western"
    )

    private val nameToIdMap = genreMap.entries.associate { (id, name) -> name to id }

    fun namesFor(ids: List<Int>): List<String> = ids.mapNotNull { genreMap[it] }

    fun idFor(name: String): Int? = nameToIdMap[name]

    fun allGenres(): List<Pair<Int, String>> = genreMap.entries.map { (id, name) -> id to name }.sortedBy { it.second }
}