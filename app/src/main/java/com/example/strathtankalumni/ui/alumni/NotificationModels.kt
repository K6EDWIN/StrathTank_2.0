// megre branch ]/StrathTank_2.0-merge/app/src/main/java/com/example/strathtankalumni/ui/alumni/NotificationModels.kt
package com.example.strathtankalumni.ui.alumni

import androidx.compose.ui.graphics.vector.ImageVector

// This class represents a single item in the notification list
data class NotificationItemData(
    val id: String, // Unique ID (e.g., connection ID, message ID, collab ID)
    val icon: ImageVector,
    val title: String,
    val subtitle: String, // Will be used for "4h ago" or description
    val type: NotificationType,
    val referenceId: String? = null // e.g., the sender's UserID or the CollaborationID
)

// This enum helps us decide what to do when a notification is clicked
enum class NotificationType {
    CONNECTION_REQUEST,
    NEW_MESSAGE,
    EVENT,
    COLLABORATION_REQUEST // ✅ ADD THIS LINE
}