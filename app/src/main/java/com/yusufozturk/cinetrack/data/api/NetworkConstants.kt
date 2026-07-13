package com.yusufozturk.cinetrack.data.api

object NetworkConstants {

    const val TMDB_BASE_URL = "https://api.themoviedb.org/3/"
    const val TMDB_WEBSITE_URL = "https://www.themoviedb.org"
    private const val TMDB_IMAGE_BASE_URL = "https://image.tmdb.org/t/p"
    const val YOUTUBE_WATCH_BASE_URL = "https://www.youtube.com/watch?v="

    fun backdropUrl(path: String?): String = "$TMDB_IMAGE_BASE_URL/w780$path"

    fun posterUrl(path: String?, size: String = "w342"): String = "$TMDB_IMAGE_BASE_URL/$size$path"

    fun profileUrl(path: String?): String = "$TMDB_IMAGE_BASE_URL/w185$path"

    fun youtubeUrl(videoKey: String): String = "$YOUTUBE_WATCH_BASE_URL$videoKey"

    fun tmdbPage(path: String): String = "$TMDB_WEBSITE_URL$path"
}
