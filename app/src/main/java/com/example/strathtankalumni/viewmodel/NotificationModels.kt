package com.example.strathtankalumni.viewmodel

import androidx.compose.ui.graphics.vector.ImageVector

enum class NotificationType {
    CONNECTION_REQUEST,
    COLLABORATION_REQUEST,
    NEW_MESSAGE,
    SYSTEM_UPDATE // ✅ Added for generic updates
}

data class NotificationItemData(
    val id: String,
    val icon: ImageVector,
    val title: String,
    val subtitle: String,
    val type: NotificationType,
    val referenceId: String? = null,
    val imageUrl: String? = null, // ✅ Added for images/diagrams support
    val timestamp: Long = System.currentTimeMillis()
)