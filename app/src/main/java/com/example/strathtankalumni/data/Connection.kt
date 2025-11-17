package com.example.strathtankalumni.data

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

// Defines the status of a connection for the UI
enum class ConnectionStatus {
    NONE, // No connection exists
    PENDING_SENT, // You sent a request
    PENDING_RECEIVED, // You received a request
    ACCEPTED
}

// Represents the document in your Firebase 'connections' collection
data class Connection(
    @DocumentId
    val id: String = "",
    val participantIds: List<String> = emptyList(),
    val senderId: String = "", // The ID of the user who sent the request
    val status: String = "pending", // "pending", "accepted", "declined"
    @ServerTimestamp
    val lastUpdated: Date? = null
)