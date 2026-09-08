package com.example.sync.data.local

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.onStart

/**
 * Manages secure storage of authentication data using [EncryptedSharedPreferences].
 * Provides reactive [Flow] streams to observe authentication state changes.
 */
class AuthManager(context: Context) {
    // Encrypted storage for sensitive data like JWT tokens
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        "auth_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    companion object {
        private const val AUTH_TOKEN = "auth_token"
        private const val USER_ID = "user_id"
        private const val USER_NAME = "user_name"
        private const val USER_ROLE = "user_role"
    }

    /**
     * Emits the current authentication token and updates whenever it changes.
     */
    val authToken: Flow<String?> = callbackFlow {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { p, key ->
            if (key == AUTH_TOKEN || key == null) trySend(p.getString(AUTH_TOKEN, null))
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        trySend(prefs.getString(AUTH_TOKEN, null))
        awaitClose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }.onStart { emit(prefs.getString(AUTH_TOKEN, null)) }

    /**
     * Emits the current user's ID.
     */
    val userId: Flow<Int> = callbackFlow {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { p, key ->
            if (key == USER_ID || key == null) trySend(p.getInt(USER_ID, -1))
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        trySend(prefs.getInt(USER_ID, -1))
        awaitClose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }.onStart { emit(prefs.getInt(USER_ID, -1)) }

    /**
     * Emits the current user's role (e.g., student, admin).
     */
    val userRole: Flow<String?> = callbackFlow {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { p, key ->
            if (key == USER_ROLE || key == null) trySend(p.getString(USER_ROLE, null))
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        trySend(prefs.getString(USER_ROLE, null))
        awaitClose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }.onStart { emit(prefs.getString(USER_ROLE, null)) }

    fun getUserIdSync(): Int = prefs.getInt(USER_ID, -1)
    fun getAuthTokenSync(): String? = prefs.getString(AUTH_TOKEN, null)

    /**
     * Saves user authentication details after a successful login or registration.
     */
    fun saveAuthData(token: String, id: Int, name: String, role: String) {
        prefs.edit().apply {
            putString(AUTH_TOKEN, token)
            putInt(USER_ID, id)
            putString(USER_NAME, name)
            putString(USER_ROLE, role)
            apply()
        }
    }

    /**
     * Clears all stored authentication data (Logout).
     */
    fun clearAuthData() {
        prefs.edit()
            .remove(AUTH_TOKEN)
            .remove(USER_ID)
            .remove(USER_NAME)
            .remove(USER_ROLE)
            .apply()
    }
}
