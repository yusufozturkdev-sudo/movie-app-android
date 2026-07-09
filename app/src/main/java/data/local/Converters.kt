package com.yusufozturk.cinetrack.data.local

import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun fromGenreIds(ids: List<Int>): String {
        return ids.joinToString(separator = ",")
    }

    @TypeConverter
    fun toGenreIds(data: String): List<Int> {
        if (data.isBlank()) return emptyList()
        return data.split(",").mapNotNull { it.toIntOrNull() }
    }
}