package com.harvey.gamespc.utils

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AnonymousIdManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private const val PREFS_NAME = "AppPrefs"
    private const val KEY_UNIQUE_ID = "unique_id"

    private fun getPreferences(): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun getAnonymousId(): String {
        val prefs = getPreferences()
        var uniqueId = prefs.getString(KEY_UNIQUE_ID, null)
        if (uniqueId == null) {
            uniqueId = UUID.randomUUID().toString()
            prefs.edit().putString(KEY_UNIQUE_ID, uniqueId).apply()
        }
        return uniqueId!!
    }
}
