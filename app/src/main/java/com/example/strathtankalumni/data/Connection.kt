package com.example.strathtankalumni.data

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

// connection
enum class ConnectionStatus {
    NONE, // No connection exists
    PENDING_SENT, // You sent a request
    PENDING_RECEIVED, // You received a request
    ACCEPTED
}

@Serializable
data class Connection(
    val id: String = "",

    @SerialName("participant_ids")
    val participantIds: List<String> = emptyList(),

    @SerialName("sender_id")
    val senderId: String = "", // The ID of the user who sent the request

    val status: String = "pending", // "pending", "accepted", "declined"

    // ✅ CHANGED: Supabase returns timestamps as ISO 8601 Strings
    @SerialName("last_updated")
    val lastUpdated: String? = null
)