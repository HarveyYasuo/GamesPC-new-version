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
    private val typingRef = databaseReference.child("typing")

    // Solo se cargan los últimos mensajes para no leer todo el historial
    private val MAX_MESSAGES = 200

    fun getMessages(): Flow<List<Message>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val messageList = mutableListOf<Message>()
                for (messageSnapshot in snapshot.children) {
                    val message = messageSnapshot.getValue(Message::class.java)
                    message?.let { 
                        messageList.add(it.copy(id = messageSnapshot.key ?: "")) 
                    }
                }
                trySend(messageList.sortedBy { it.timestamp })
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        chatRef.orderByKey().limitToLast(MAX_MESSAGES).addValueEventListener(listener)
        awaitClose { chatRef.removeEventListener(listener) }
    }

    fun markMessageAsRead(messageId: String) {
        if (messageId.isNotEmpty()) {
            chatRef.child(messageId).child("status").setValue(MessageStatus.READ)
        }
    }

    fun setTypingStatus(userId: String, userName: String, isTyping: Boolean) {
        if (isTyping) {
            typingRef.child(userId).setValue(userName)
        } else {
            typingRef.child(userId).removeValue()
        }
    }

    fun getTypingUsers(currentUserId: String): Flow<List<String>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val typingUsers = mutableListOf<String>()
                for (child in snapshot.children) {
                    val userId = child.key
                    val userName = child.getValue(String::class.java)
                    if (userId != currentUserId && userName != null) {
                        typingUsers.add(userName)
                    }
                }
                trySend(typingUsers)
            }

            override fun onCancelled(error: DatabaseError) {}
        }
        typingRef.addValueEventListener(listener)
        awaitClose { typingRef.removeEventListener(listener) }
    }

    fun sendMessage(message: Message) {
        chatRef.push().setValue(message)
    }
}
