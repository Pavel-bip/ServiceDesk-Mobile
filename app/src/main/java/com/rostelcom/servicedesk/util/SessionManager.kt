package com.rostelcom.servicedesk.util

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys

class SessionManager(context: Context) {

    private val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)

    private val prefs: SharedPreferences = EncryptedSharedPreferences.create(
        "secure_session",
        masterKeyAlias,
        context,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    var isLoggedIn: Boolean
        get() = prefs.getBoolean("logged_in", false)
        set(value) = prefs.edit().putBoolean("logged_in", value).apply()

    var userEmail: String
        get() = prefs.getString("email", "") ?: ""
        set(value) = prefs.edit().putString("email", value).apply()

    var userId: String
        get() = prefs.getString("uid", "") ?: ""
        set(value) = prefs.edit().putString("uid", value).apply()

    var lastActiveTime: Long
        get() = prefs.getLong("last_active", 0L)
        set(value) = prefs.edit().putLong("last_active", value).apply()

    var isDarkTheme: Boolean
        get() = prefs.getBoolean("dark_theme", false)
        set(value) = prefs.edit().putBoolean("dark_theme", value).apply()

    var biometricEnabled: Boolean
        get() = prefs.getBoolean("biometric_enabled", false)
        set(value) = prefs.edit().putBoolean("biometric_enabled", value).apply()

    fun logout() {
        val keepDarkTheme = isDarkTheme
        val keepBiometric = biometricEnabled
        prefs.edit().clear().apply()
        isDarkTheme = keepDarkTheme
        biometricEnabled = keepBiometric
    }
}