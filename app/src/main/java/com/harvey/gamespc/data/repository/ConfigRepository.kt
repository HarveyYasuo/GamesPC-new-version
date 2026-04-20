package com.harvey.gamespc.data.repository

import com.google.firebase.database.DatabaseReference
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ConfigRepository @Inject constructor(
    private val databaseReference: DatabaseReference
) {
    suspend fun getMinRequiredVersionCode(): Long {
        val versionRef = databaseReference.child("config/version/minRequiredVersionCode")
        val snapshot = versionRef.get().await()
        return snapshot.getValue(Long::class.java) ?: -1
    }
}
