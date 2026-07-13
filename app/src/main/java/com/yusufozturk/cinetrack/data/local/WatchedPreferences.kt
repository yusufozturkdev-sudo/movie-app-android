package com.yusufozturk.cinetrack.data.local

import android.content.Context

class WatchedPreferences(context: Context) {

    private val prefs = context.getSharedPreferences("cinetrack_watched", Context.MODE_PRIVATE)
    private val key = "watched_movie_ids"

    fun getWatchedIds(): Set<Int> {
        return prefs.getStringSet(key, emptySet())?.mapNotNull { it.toIntOrNull() }?.toSet() ?: emptySet()
    }

    fun toggle(movieId: Int) {
        val current = getWatchedIds().toMutableSet()
        if (current.contains(movieId)) {
            current.remove(movieId)
        } else {
            current.add(movieId)
        }
        prefs.edit().putStringSet(key, current.map { it.toString() }.toSet()).apply()
    }
}
