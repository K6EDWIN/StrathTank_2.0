package com.example.strathtankalumni.ui.alumni

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.strathtankalumni.data.Connection
import com.example.strathtankalumni.data.User
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
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
    // Map: userId -> count (e.g., "user123": 2)
    val unreadCount: Map<String, Long> = emptyMap()
)

data class ConversationWithUser(
    val conversation: Conversation,
    val user: User,
    val unreadCount: Int = 0
)

class MessagesViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()

    // Inbox State
    private val _conversations = MutableStateFlow<List<ConversationWithUser>>(emptyList())
    val conversations = _conversations.asStateFlow()

    // Search
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    // Filter Logic
    val filteredConversations = combine(_conversations, _searchQuery) { convos, query ->
        if (query.isBlank()) convos
        else convos.filter {
            val fullName = "${it.user.firstName} ${it.user.lastName}"
            fullName.contains(query, ignoreCase = true)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Chat Screen Messages
    private val _directMessages = MutableStateFlow<List<Message>>(emptyList())
    val directMessages = _directMessages.asStateFlow()

    // ✅ NEW: Holds the profile of the person we are currently chatting with
    private val _activeChatPartner = MutableStateFlow<User?>(null)
    val activeChatPartner = _activeChatPartner.asStateFlow()

    // Listeners (to clean up)
    private var inboxListener: ListenerRegistration? = null
    private var chatListener: ListenerRegistration? = null

    // --- 1. INBOX LOGIC ---

    fun loadConversations(currentUserId: String, connections: List<Connection>? = null) {
        if (inboxListener != null) return

        inboxListener = db.collection("chats")
            .whereArrayContains("participants", currentUserId)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e("MessagesViewModel", "Error loading inbox", e)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    viewModelScope.launch {
                        val loadedConversations = mutableListOf<ConversationWithUser>()

                        for (doc in snapshot.documents) {
                            val conversation = doc.toObject(Conversation::class.java)?.copy(id = doc.id)

                            if (conversation != null) {
                                val otherUserId = conversation.participants.firstOrNull { it != currentUserId }

                                if (otherUserId != null) {
                                    val user = fetchUserProfile(otherUserId)

                                    if (user != null) {
                                        val myUnread = conversation.unreadCount[currentUserId]?.toInt() ?: 0
                                        loadedConversations.add(
                                            ConversationWithUser(conversation, user, myUnread)
                                        )
                                    }
                                }
                            }
                        }
                        _conversations.value = loadedConversations.sortedByDescending {
                            it.conversation.lastMessageTimestamp
                        }
                    }
                }
            }
    }

    private suspend fun fetchUserProfile(userId: String): User? {
        return try {
            val doc = db.collection("users").document(userId).get().await()
            doc.toObject(User::class.java)
        } catch (e: Exception) {
            null
        }
    }

    // --- 2. CHAT SCREEN LOGIC ---

    fun loadDirectMessages(currentUserId: String, otherUserId: String) {
        // Remove old listener if switching chats
        chatListener?.remove()

        // ✅ NEW: Fetch the Chat Partner's Profile immediately
        viewModelScope.launch {
            val user = fetchUserProfile(otherUserId)
            _activeChatPartner.value = user
        }

        val chatId = getChatId(currentUserId, otherUserId)

        chatListener = db.collection("chats").document(chatId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshots, error ->
                if (error != null) return@addSnapshotListener

                val messagesList = snapshots?.documents?.mapNotNull {
                    it.toObject(Message::class.java)?.copy(id = it.id)
                } ?: emptyList()

                _directMessages.value = messagesList
            }
    }

    fun clearDirectMessages() {
        _directMessages.value = emptyList()
        _activeChatPartner.value = null // ✅ Clear the partner info
        chatListener?.remove()
        chatListener = null
    }

    // --- 3. ACTIONS (Send/Read) ---

    fun sendMessage(text: String, senderId: String, receiverId: String) {
        if (text.isBlank()) return

        val chatId = getChatId(senderId, receiverId)
        val chatRef = db.collection("chats").document(chatId)
        val messagesRef = chatRef.collection("messages")

        val newMessage = Message(
            text = text,
            senderId = senderId,
            receiverId = receiverId,
            timestamp = null
        )

        db.runBatch { batch ->
            val newMsgDoc = messagesRef.document()
            batch.set(newMsgDoc, newMessage)

            val chatData = mapOf(
                "participants" to listOf(senderId, receiverId),
                "lastMessage" to text,
                "lastSenderId" to senderId,
                "lastMessageTimestamp" to FieldValue.serverTimestamp(),
                "unreadCount.$receiverId" to FieldValue.increment(1)
            )

            batch.set(chatRef, chatData, SetOptions.merge())
        }.addOnFailureListener { e ->
            Log.e("MessagesViewModel", "Failed to send message", e)
        }
    }

    fun markAsRead(currentUserId: String, otherUserId: String) {
        val chatId = getChatId(currentUserId, otherUserId)
        val chatRef = db.collection("chats").document(chatId)
        val updates = mapOf("unreadCount.$currentUserId" to 0)
        chatRef.update(updates).addOnFailureListener { }
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    private fun getChatId(userId1: String, userId2: String): String {
        return if (userId1 < userId2) "${userId1}_${userId2}" else "${userId2}_${userId1}"
    }

    override fun onCleared() {
        super.onCleared()
        inboxListener?.remove()
        chatListener?.remove()
    }
}