package com.yusufozturk.cinetrack.data.local

import android.content.Context

class SearchHistoryPreferences(context: Context) {

    private val prefs = context.getSharedPreferences("cinetrack_search_history", Context.MODE_PRIVATE)
    private val key = "recent_searches"
    private val maxEntries = 8

    fun getHistory(): List<String> {
        val raw = prefs.getString(key, null) ?: return emptyList()
        return raw.split("||").filter { it.isNotBlank() }
    }

    fun addSearch(query: String) {
        val trimmed = query.trim()
        if (trimmed.isBlank()) return

        val current = getHistory().toMutableList()
        current.removeAll { it.equals(trimmed, ignoreCase = true) }
        current.add(0, trimmed)

        val limited = current.take(maxEntries)
        prefs.edit().putString(key, limited.joinToString("||")).apply()
    }

    fun clearHistory() {
        prefs.edit().remove(key).apply()
    }
}