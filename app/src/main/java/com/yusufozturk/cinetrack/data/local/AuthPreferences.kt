package com.yusufozturk.cinetrack.data.local

import android.content.Context
import androidx.core.content.edit

class AuthPreferences(context: Context) {

    private val prefs = context.getSharedPreferences("cinetrack_auth", Context.MODE_PRIVATE)

    var sessionId: String?
        get() = prefs.getString("session_id", null)
        set(value) {
            prefs.edit { putString("session_id", value) }
        }

    var accountId: Int
        get() = prefs.getInt("account_id", -1)
        set(value) {
            prefs.edit { putInt("account_id", value) }
        }

    fun isLoggedIn(): Boolean = sessionId != null && accountId != -1

    fun clear() {
        prefs.edit { clear() }
    }
}