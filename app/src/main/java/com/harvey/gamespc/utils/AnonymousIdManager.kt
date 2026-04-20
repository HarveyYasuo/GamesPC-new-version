package com.harvey.gamespc.utils

import android.content.Context
import android.content.SharedPreferences
import java.util.UUID

object AnonymousIdManager {

    private const val PREFS_NAME = "AppPrefs"
    private const val KEY_UNIQUE_ID = "unique_id"

    private fun getPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun getUniqueId(context: Context): String {
        val prefs = getPreferences(context)
        var uniqueId = prefs.getString(KEY_UNIQUE_ID, null)
        if (uniqueId == null) {
            uniqueId = UUID.randomUUID().toString()
            prefs.edit().putString(KEY_UNIQUE_ID, uniqueId).apply()
        }
        return uniqueId
    }
}
