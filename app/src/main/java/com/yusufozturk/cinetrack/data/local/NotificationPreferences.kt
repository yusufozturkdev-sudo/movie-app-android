package com.yusufozturk.cinetrack.data.local

import android.content.Context

class NotificationPreferences(context: Context) {

    private val prefs = context.getSharedPreferences("cinetrack_notifications", Context.MODE_PRIVATE)

    var newReleasesEnabled: Boolean
        get() = prefs.getBoolean("new_releases", true)
        set(value) = prefs.edit().putBoolean("new_releases", value).apply()

    var watchlistRemindersEnabled: Boolean
        get() = prefs.getBoolean("watchlist_reminders", true)
        set(value) = prefs.edit().putBoolean("watchlist_reminders", value).apply()

    var trailerAlertsEnabled: Boolean
        get() = prefs.getBoolean("trailer_alerts", false)
        set(value) = prefs.edit().putBoolean("trailer_alerts", value).apply()
}
