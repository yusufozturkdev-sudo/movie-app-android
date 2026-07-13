package com.yusufozturk.cinetrack.data.model

object RatingFormatter {
    fun format(rating: Double): String {
        return String.format("%.1f", rating)
    }

    fun formatRuntime(minutes: Int): String {
        val hours = minutes / 60
        val mins = minutes % 60
        return "${hours}h ${mins}m"
    }
}
