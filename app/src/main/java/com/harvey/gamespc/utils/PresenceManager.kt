package com.harvey.gamespc.utils

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PresenceManager @Inject constructor(
    private val databaseReference: DatabaseReference,
    private val anonymousIdManager: AnonymousIdManager
) {
    private val presenceRef = databaseReference.child("presence")

    private val _activeUsersCount = MutableStateFlow(0)
    val activeUsersCount = _activeUsersCount.asStateFlow()

    private val _onlineUsers = MutableStateFlow<Set<String>>(emptySet())
    val onlineUsers = _onlineUsers.asStateFlow()

    private val presenceListener = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            val count = snapshot.childrenCount.toInt()
            _activeUsersCount.value = count
            
            val users = mutableSetOf<String>()
            for (child in snapshot.children) {
                child.key?.let { users.add(it) }
            }
            _onlineUsers.value = users
        }

        override fun onCancelled(error: DatabaseError) {}
    }

    init {
        presenceRef.addValueEventListener(presenceListener)
    }

    fun goOnline() {
        val uniqueId = anonymousIdManager.getAnonymousId()
        val userStatusRef = presenceRef.child(uniqueId)
        userStatusRef.setValue(System.currentTimeMillis()) // Guardamos el timestamp de conexión
        userStatusRef.onDisconnect().removeValue()
    }

    fun goOffline() {
        val uniqueId = anonymousIdManager.getAnonymousId()
        presenceRef.child(uniqueId).removeValue()
    }
}
