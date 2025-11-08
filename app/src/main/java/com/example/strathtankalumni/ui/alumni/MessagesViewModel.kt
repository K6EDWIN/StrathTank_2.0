package com.example.strathtankalumni.data

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ServerTimestamp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Date

// ✅ 1. ADDED IMPORTS for User and Connection
import com.example.strathtankalumni.data.User
import com.example.strathtankalumni.data.Connection


// --- DATA MODELS ---

/**
 * Data class mapping to the 'users' collection in Firestore.
 */

data class Message(
    val id: String = "",
    val text: String = "",
    val senderId: String = "",
    val receiverId: String = "",
    @ServerTimestamp
    val timestamp: Date? = null
)

/**
 * Represents a conversation summary in the top-level collection.
 * Firestore Path: /chats/{chatId}
 */
data class Conversation(
    val id: String = "", // Composite ID e.g., "id1_id2"
    val participants: List<String> = emptyList(),
    val lastMessage: String = "",
    val lastSenderId: String = "",
    @ServerTimestamp
    val lastMessageTimestamp: Date? = null
)

/**
 * A helper class for the UI (not stored in Firestore).
 * It combines a Conversation with the other user's details for the inbox screen.
 */
data class ConversationWithUser(
    val conversation: Conversation,
    val user: User
)


// --- VIEWMODEL ---

class MessagesViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()

    // State for the main conversation list (Inbox)
    private val _conversations = MutableStateFlow<List<ConversationWithUser>>(emptyList())
    val conversations = _conversations.asStateFlow()

    // State for the messages within a single chat screen
    private val _directMessages = MutableStateFlow<List<Message>>(emptyList())
    val directMessages = _directMessages.asStateFlow()

    // Helper to generate a consistent chat ID
    private fun getChatId(userId1: String, userId2: String): String {
        return if (userId1 < userId2) "${userId1}_${userId2}" else "${userId2}_${userId1}"
    }

    /**
     * ✅ 2. REPLACED with the new function that uses Connections
     * This function now loads conversations
     * based on the list of accepted connections from AuthViewModel.
     */
    fun loadConversations(currentUserId: String, connections: List<Connection>) {
        viewModelScope.launch {
            try {
                // Map the list of accepted connections to ConversationWithUser objects
                val convosWithUsers = connections.mapNotNull { connection ->
                    // Find the *other* user's ID from the connection
                    val otherUserId = connection.participantIds.firstOrNull { it != currentUserId }
                    if (otherUserId != null) {
                        // Fetch the other user's data
                        val userDoc = db.collection("users").document(otherUserId).get().await()
                        val user = userDoc.toObject(User::class.java)

                        if (user != null) {
                            // Fetch the last message for this chat
                            val chatId = connection.id // The connection ID is the chat ID
                            val lastMessageDoc = db.collection("chats").document(chatId)
                                .collection("messages")
                                .orderBy("timestamp", Query.Direction.DESCENDING)
                                .limit(1)
                                .get()
                                .await()

                            val lastMessage = lastMessageDoc.documents.firstOrNull()
                                ?.toObject(Message::class.java)

                            // Create a Conversation summary object
                            val convo = Conversation(
                                id = chatId,
                                participants = connection.participantIds,
                                lastMessage = lastMessage?.text ?: "No messages yet.",
                                lastSenderId = lastMessage?.senderId ?: "",
                                lastMessageTimestamp = lastMessage?.timestamp
                            )
                            return@mapNotNull ConversationWithUser(convo, user)
                        }
                    }
                    null // Return null if any step fails, mapNotNull will filter it out
                }

                // Sort by last message timestamp, newest first
                _conversations.value = convosWithUsers.sortedByDescending {
                    it.conversation.lastMessageTimestamp
                }

            } catch (e: Exception) {
                Log.e("MessagesViewModel", "Error loading conversations", e)
                _conversations.value = emptyList()
            }
        }
    }

    /**
     * Loads the messages for a specific chat between two users.
     */
    fun loadDirectMessages(currentUserId: String, otherUserId: String) {
        val chatId = getChatId(currentUserId, otherUserId)
        db.collection("chats").document(chatId).collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshots, error ->
                if (error != null || snapshots == null) {
                    // Handle error
                    return@addSnapshotListener
                }

                // Map documents to Message objects
                val messagesList = snapshots.documents.mapNotNull {
                    it.toObject(Message::class.java)?.copy(id = it.id)
                }
                _directMessages.value = messagesList
            }
    }

    /**
     * Clears the direct messages list, useful when navigating away from a chat.
     */
    fun clearDirectMessages() {
        _directMessages.value = emptyList()
    }

    /**
     * Sends a message and updates the conversation summary in one transaction.
     */
    fun sendMessage(text: String, senderId: String, receiverId: String) {
        viewModelScope.launch {
            val chatId = getChatId(senderId, receiverId)

            val newMessage = Message(
                text = text,
                senderId = senderId,
                receiverId = receiverId,
                timestamp = null // Set to null, @ServerTimestamp will replace it
            )

            // Use a batch write for atomicity
            val batch = db.batch()

            // 1. Add the new message to the subcollection
            val newMessageRef = db.collection("chats").document(chatId)
                .collection("messages").document() // Auto-generates ID
            batch.set(newMessageRef, newMessage)

            // 2. Update the main conversation document
            val chatDocRef = db.collection("chats").document(chatId)
            val conversationUpdate = mapOf(
                "participants" to listOf(senderId, receiverId),
                "lastMessage" to text,
                "lastSenderId" to senderId,
                "lastMessageTimestamp" to FieldValue.serverTimestamp()
            )
            // Use SetOptions.merge() to create the doc if it doesn't exist
            batch.set(chatDocRef, conversationUpdate, SetOptions.merge())

            try {
                batch.commit().await()
            } catch (e: Exception) {
                Log.e("MessagesViewModel", "Error sending message", e)
            }
        }
    }
}