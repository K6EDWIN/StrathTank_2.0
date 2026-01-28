package com.example.strathtankalumni.data

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class Comment(
    val id: String = "",
    val text: String = "",
    val userId: String = "",
    val userName: String = "",
    val userPhotoUrl: String = "",

    // ✅ ADDED: Required to link the comment to a project (used in AuthViewModel)
    @SerialName("project_id")
    val projectId: String = "",

    // ✅ CHANGED: Supabase returns timestamps as ISO 8601 Strings
    @SerialName("created_at")
    val createdAt: String? = null
)