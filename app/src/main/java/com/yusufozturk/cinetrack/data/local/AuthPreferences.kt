package com.yusufozturk.cinetrack.data.local

import android.content.Context

class AuthPreferences(context: Context) {

    private val prefs = context.getSharedPreferences("cinetrack_auth", Context.MODE_PRIVATE)

    var sessionId: String?
        get() = prefs.getString("session_id", null)
        set(value) {
            prefs.edit().putString("session_id", value).apply()
        }

    var accountId: Int
        get() = prefs.getInt("account_id", -1)
        set(value) {
            prefs.edit().putInt("account_id", value).apply()
        }

    fun isLoggedIn(): Boolean = sessionId != null && accountId != -1

    fun clear() {
        prefs.edit().clear().apply()
    }
}
