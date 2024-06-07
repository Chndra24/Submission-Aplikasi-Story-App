package com.cwb.storyapp.data

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREFS_NAME = "session_prefs"
        private const val KEY_AUTH_TOKEN = "auth_token"
    }

    fun saveAuthToken(token: String) {
        val editor = prefs.edit()
        editor.putString(KEY_AUTH_TOKEN, token)
        editor.apply()
    }

    fun fetchAuthToken(): String? {
        return prefs.getString(KEY_AUTH_TOKEN, null)
    }

    fun isLoggedIn(): Boolean {
        return fetchAuthToken() != null
    }

    fun clearSession() {
        val editor = prefs.edit()
        editor.remove(KEY_AUTH_TOKEN)
        editor.apply()
    }
}