package com.example.strathtankalumni.data

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class Collaboration(
    val id: String = "",
    val projectId: String = "",
    val projectTitle: String = "",
    val projectDescription: String = "",
    val projectImageUrl: String = "",
    val projectOwnerId: String = "",
    val collaboratorId: String = "",
    val collaboratorName: String = "", // Name of the person requesting
    val collaboratorPhotoUrl: String = "", // Photo of the person requesting
    val status: String = "pending", // "pending", "accepted", "declined", "left"

    // Supabase returns timestamps as ISO 8601 strings (e.g., "2023-12-01T10:00:00Z")
    // Map these to the correct column names in your Supabase table if they differ (e.g., created_at)
    @SerialName("created_at")
    val requestedAt: String? = null,

    @SerialName("updated_at")
    val updatedAt: String? = null
)

// ✅ ADDED: New Data Class for the Discussion Feature
@Serializable
data class ProjectComment(
    val id: String = "",
    val projectId: String = "", // Links comment to the specific project
    val userId: String = "",
    val userName: String = "",
    val userPhotoUrl: String? = null,
    val text: String = "",

    @SerialName("created_at")
    val timestamp: String? = null
)