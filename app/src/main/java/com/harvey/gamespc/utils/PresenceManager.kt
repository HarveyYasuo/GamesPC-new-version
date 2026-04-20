package com.harvey.gamespc.utils

import android.content.Context
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

object PresenceManager {

    private val database = FirebaseDatabase.getInstance()
    private val presenceRef = database.getReference("presence")

    private val _activeUsersCount = MutableStateFlow(0)
    val activeUsersCount = _activeUsersCount.asStateFlow()

    private val presenceListener = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            val count = snapshot.childrenCount.toInt()
            _activeUsersCount.value = count
        }

        override fun onCancelled(error: DatabaseError) {
            // Handle error
        }
    }

    init {
        presenceRef.addValueEventListener(presenceListener)
    }

    fun goOnline(context: Context) {
        val uniqueId = AnonymousIdManager.getUniqueId(context)
        val userStatusRef = presenceRef.child(uniqueId)
        userStatusRef.setValue(true)
        userStatusRef.onDisconnect().removeValue()
    }

    fun goOffline(context: Context) {
        val uniqueId = AnonymousIdManager.getUniqueId(context)
        presenceRef.child(uniqueId).removeValue()
    }
}
