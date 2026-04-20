package com.harvey.gamespc.data.repository

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.harvey.gamespc.ui.screens.Message
import com.harvey.gamespc.ui.screens.MessageStatus
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatRepository @Inject constructor(
    private val databaseReference: DatabaseReference
) {
    private val chatRef = databaseReference.child("chat")

    fun getMessages(): Flow<List<Message>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val messageList = mutableListOf<Message>()
                for (messageSnapshot in snapshot.children) {
                    val message = messageSnapshot.getValue(Message::class.java)
                    message?.let { messageList.add(it) }
                }
                trySend(messageList.sortedBy { it.timestamp })
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        chatRef.addValueEventListener(listener)
        awaitClose { chatRef.removeEventListener(listener) }
    }

    fun sendMessage(message: Message) {
        chatRef.push().setValue(message)
    }
}
