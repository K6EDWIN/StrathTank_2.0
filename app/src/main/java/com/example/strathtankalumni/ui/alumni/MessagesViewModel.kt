package com.example.strathtankalumni.ui.alumni

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ServerTimestamp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Date
import com.example.strathtankalumni.data.User
import com.example.strathtankalumni.data.Connection

data class Message(
    val id: String = "",
    val text: String = "",
    val senderId: String = "",
    val receiverId: String = "",
    @ServerTimestamp
    val timestamp: Date? = null
)

data class Conversation(
    val id: String = "",
    val participants: List<String> = emptyList(),
    val lastMessage: String = "",
    val lastSenderId: String = "",
    @ServerTimestamp
    val lastMessageTimestamp: Date? = null,
    // This map stores the unread count
    val unreadCount: Map<String, Long> = emptyMap()
)

data class ConversationWithUser(
    val conversation: Conversation,
    val user: User,
    // This holds the count for the current user
    val unreadCount: Int = 0
)

class MessagesViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()

    // (Inbox) - The raw list of conversations
    private val _conversations = MutableStateFlow<List<ConversationWithUser>>(emptyList())
    val conversations = _conversations.asStateFlow()

    // Search Query State
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    // Filtered Conversations based on Search Query
    val filteredConversations = combine(_conversations, _searchQuery) { convos, query ->
        if (query.isBlank()) {
            convos
        } else {
            convos.filter {
                val fullName = "${it.user.firstName} ${it.user.lastName}"
                fullName.contains(query, ignoreCase = true)
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // State for the messages within a single chat screen
    private val _directMessages = MutableStateFlow<List<Message>>(emptyList())
    val directMessages = _directMessages.asStateFlow()

    // Helper to generate a consistent chat ID
    private fun getChatId(userId1: String, userId2: String): String {
        return if (userId1 < userId2) "${userId1}_${userId2}" else "${userId2}_${userId1}"
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

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
                            // 1. FETCH THE CHAT DOCUMENT ITSELF
                            // Ideally, Connection ID should match Chat ID.
                            // If not, construct it using getChatId logic if you enforced that rule
                            // but here we use connection.id as per your previous logic.
                            val chatId = connection.id
                            val chatDoc = db.collection("chats").document(chatId).get().await()
                            val conversation = chatDoc.toObject(Conversation::class.java)

                            // 2. GET THE CURRENT USER'S UNREAD COUNT
                            val unreadCountForMe = conversation?.unreadCount?.get(currentUserId) ?: 0L

                            // 3. CREATE THE CONVERSATION OBJECT
                            val convo = conversation ?: Conversation(
                                id = chatId,
                                participants = connection.participantIds,
                                lastMessage = "No messages yet."
                            )

                            // 4. RETURN THE NEW HELPER CLASS
                            return@mapNotNull ConversationWithUser(
                                conversation = convo,
                                user = user,
                                unreadCount = unreadCountForMe.toInt()
                            )
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

    fun loadDirectMessages(currentUserId: String, otherUserId: String) {
        val chatId = getChatId(currentUserId, otherUserId)
        db.collection("chats").document(chatId).collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshots, error ->
                if (error != null || snapshots == null) {
                    return@addSnapshotListener
                }

                // Map documents to Message objects
                val messagesList = snapshots.documents.mapNotNull {
                    it.toObject(Message::class.java)?.copy(id = it.id)
                }
                _directMessages.value = messagesList
            }
    }

    fun clearDirectMessages() {
        _directMessages.value = emptyList()
    }

    fun markAsRead(currentUserId: String, otherUserId: String) {
        if (currentUserId.isBlank()) return

        val chatId = getChatId(currentUserId, otherUserId)
        val chatDocRef = db.collection("chats").document(chatId)
        val unreadResetKey = "unreadCount.$currentUserId"

        // Set the count to 0.
        chatDocRef.update(unreadResetKey, 0)
            .addOnFailureListener {
                Log.e("MessagesViewModel", "Failed to mark as read", it)
            }
    }

    fun sendMessage(text: String, senderId: String, receiverId: String) {
        viewModelScope.launch {
            val chatId = getChatId(senderId, receiverId)

            val newMessage = Message(
                text = text,
                senderId = senderId,
                receiverId = receiverId,
                timestamp = null
            )

            val batch = db.batch()

            // 1. Add the new message to the subcollection
            val newMessageRef = db.collection("chats").document(chatId)
                .collection("messages").document()
            batch.set(newMessageRef, newMessage)

            // 2. Update the main conversation document
            val chatDocRef = db.collection("chats").document(chatId)

            val conversationSummaryUpdate = mapOf(
                "participants" to listOf(senderId, receiverId),
                "lastMessage" to text,
                "lastSenderId" to senderId,
                "lastMessageTimestamp" to FieldValue.serverTimestamp()
            )

            batch.set(chatDocRef, conversationSummaryUpdate, SetOptions.merge())

            val unreadIncrementKey = "unreadCount.$receiverId"
            batch.update(chatDocRef, unreadIncrementKey, FieldValue.increment(1))

            try {
                batch.commit().await()
            } catch (e: Exception) {
                Log.e("MessagesViewModel", "Error sending message", e)
            }
        }
    }
}