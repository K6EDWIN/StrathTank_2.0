package com.example.strathtankalumni.viewmodel

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.strathtankalumni.data.User
import com.example.strathtankalumni.util.Supabase
import io.github.jan.supabase.annotations.SupabaseExperimental
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.realtime.realtime
import io.github.jan.supabase.realtime.selectAsFlow
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.Instant // ✅ Changed to Java time to avoid extra dependencies
import java.util.UUID

// --- DATA CLASSES (Supabase Compatible) ---

@Serializable
data class Message(
    val id: String = "",

    @SerialName("chat_id")
    val chatId: String = "",

    val text: String = "",

    @SerialName("sender_id")
    val senderId: String = "",

    @SerialName("receiver_id")
    val receiverId: String = "",

    @SerialName("created_at")
    val timestamp: String? = null
)

@Serializable
data class Conversation(
    val id: String = "",

    val participants: List<String> = emptyList(),

    @SerialName("last_message")
    val lastMessage: String = "",

    @SerialName("last_sender_id")
    val lastSenderId: String = "",

    @SerialName("last_message_timestamp")
    val lastMessageTimestamp: String? = null,

    @SerialName("unread_count")
    val unreadCount: Map<String, Int> = emptyMap()
)

data class ConversationWithUser(
    val conversation: Conversation,
    val user: User,
    val unreadCount: Int = 0
)

class MessagesViewModel : ViewModel() {

    private val supabase = Supabase.client

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

    // Active Chat Partner Profile
    private val _activeChatPartner = MutableStateFlow<User?>(null)
    val activeChatPartner = _activeChatPartner.asStateFlow()

    // Jobs for cancellation
    private var inboxJob: Job? = null
    private var chatJob: Job? = null

    // --- 1. INBOX LOGIC ---

    @OptIn(SupabaseExperimental::class)
    fun loadConversations(currentUserId: String) {
        if (inboxJob != null) return

        inboxJob = viewModelScope.launch {
            // ✅ FIX 1: Use supabase.postgrest.from(...)
            supabase.postgrest.from("chats").selectAsFlow(Conversation::id)
                .map { list: List<Conversation> -> // ✅ FIX 2: Explicit type
                    list.filter { it.participants.contains(currentUserId) }
                        .sortedByDescending { it.lastMessageTimestamp }
                }
                .collect { conversations ->
                    val loadedConversations = mutableListOf<ConversationWithUser>()

                    for (convo in conversations) {
                        val otherUserId = convo.participants.firstOrNull { it != currentUserId }
                        if (otherUserId != null) {
                            val user = fetchUserProfile(otherUserId)
                            if (user != null) {
                                val myUnread = convo.unreadCount[currentUserId] ?: 0
                                loadedConversations.add(ConversationWithUser(convo, user, myUnread))
                            }
                        }
                    }
                    _conversations.value = loadedConversations
                }
        }
    }

    private suspend fun fetchUserProfile(userId: String): User? {
        return try {
            supabase.postgrest.from("users").select {
                filter { eq("user_id", userId) }
            }.decodeSingleOrNull<User>()
        } catch (e: Exception) {
            null
        }
    }

    // --- 2. CHAT SCREEN LOGIC ---

    @OptIn(SupabaseExperimental::class)
    fun loadDirectMessages(currentUserId: String, otherUserId: String) {
        chatJob?.cancel()

        viewModelScope.launch {
            _activeChatPartner.value = fetchUserProfile(otherUserId)
        }

        val chatId = getChatId(currentUserId, otherUserId)

        chatJob = viewModelScope.launch {
            // ✅ FIX 1: Use supabase.postgrest.from(...)
            supabase.postgrest.from("messages").selectAsFlow(Message::id)
                .map { list: List<Message> -> // ✅ FIX 2: Explicit type
                    list.filter { it.chatId == chatId }
                        .sortedBy { it.timestamp }
                }
                .collect { messages ->
                    _directMessages.value = messages
                }
        }
    }

    fun clearDirectMessages() {
        _directMessages.value = emptyList()
        _activeChatPartner.value = null
        chatJob?.cancel()
        chatJob = null
    }

    // --- 3. ACTIONS (Send/Read) ---

    @RequiresApi(Build.VERSION_CODES.O)
    fun sendMessage(text: String, senderId: String, receiverId: String) {
        if (text.isBlank()) return

        val chatId = getChatId(senderId, receiverId)

        viewModelScope.launch {
            try {
                // 1. Insert Message
                val newMessage = Message(
                    id = UUID.randomUUID().toString(),
                    chatId = chatId,
                    text = text,
                    senderId = senderId,
                    receiverId = receiverId
                    // timestamp is auto-generated by DB, or omitted here
                )
                supabase.postgrest.from("messages").insert(newMessage)

                // 2. Update Chat Metadata
                val currentChat = supabase.postgrest.from("chats").select {
                    filter { eq("id", chatId) }
                }.decodeSingleOrNull<Conversation>()

                val currentUnreadMap = currentChat?.unreadCount?.toMutableMap() ?: mutableMapOf()
                val currentReceiverCount = currentUnreadMap[receiverId] ?: 0
                currentUnreadMap[receiverId] = currentReceiverCount + 1

                // ✅ FIX 3: Use Java Instant for time
                val now = Instant.now().toString()

                val chatUpdate = Conversation(
                    id = chatId,
                    participants = listOf(senderId, receiverId),
                    lastMessage = text,
                    lastSenderId = senderId,
                    lastMessageTimestamp = now,
                    unreadCount = currentUnreadMap
                )

                supabase.postgrest.from("chats").upsert(chatUpdate)

            } catch (e: Exception) {
                Log.e("MessagesViewModel", "Failed to send message", e)
            }
        }
    }

    fun markAsRead(currentUserId: String, otherUserId: String) {
        val chatId = getChatId(currentUserId, otherUserId)

        viewModelScope.launch {
            try {
                val currentChat = supabase.postgrest.from("chats").select {
                    filter { eq("id", chatId) }
                }.decodeSingleOrNull<Conversation>() ?: return@launch

                val currentUnreadMap = currentChat.unreadCount.toMutableMap()
                if (currentUnreadMap[currentUserId] != 0) {
                    currentUnreadMap[currentUserId] = 0

                    supabase.postgrest.from("chats").update(
                        mapOf("unread_count" to currentUnreadMap)
                    ) {
                        filter { eq("id", chatId) }
                    }
                }
            } catch (e: Exception) {
                Log.e("MessagesViewModel", "Failed to mark read", e)
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    private fun getChatId(userId1: String, userId2: String): String {
        return if (userId1 < userId2) "${userId1}_${userId2}" else "${userId2}_${userId1}"
    }

    override fun onCleared() {
        super.onCleared()
        inboxJob?.cancel()
        chatJob?.cancel()
    }
}