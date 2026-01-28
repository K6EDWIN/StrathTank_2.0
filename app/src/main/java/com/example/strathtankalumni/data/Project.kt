package com.example.strathtankalumni.data

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName
import kotlinx.serialization.Transient

// -------------------------------------
// Data class for a Project Like
// This document's ID will be projectId_userId
// -------------------------------------
@Serializable
data class ProjectLike(
    @SerialName("user_id")
    val userId: String = "",

    @SerialName("project_id")
    val projectId: String = "",

    // Supabase returns timestamps as ISO 8601 strings
    @SerialName("created_at")
    val createdAt: String? = null
)

// -------------------------------------
// Main Project data class
// -------------------------------------
@Serializable
data class Project(
    // Nullable ID allows Supabase to generate the UUID on insert
    val id: String? = null,

    @SerialName("user_id")
    val userId: String = "",

    val title: String = "",
    val description: String = "",

    @SerialName("project_url")
    val projectUrl: String = "",

    @SerialName("github_url")
    val githubUrl: String = "",

    @SerialName("project_type")
    val projectType: String = "",

    @SerialName("image_url")
    val imageUrl: String = "",

    @SerialName("media_image_urls")
    val mediaImageUrls: List<String> = emptyList(),

    @SerialName("pdf_url")
    val pdfUrl: String = "",

    val categories: List<String> = emptyList(),

    @SerialName("programming_languages")
    val programmingLanguages: List<String> = emptyList(),

    @SerialName("database_used")
    val databaseUsed: List<String> = emptyList(),

    @SerialName("tech_stack")
    val techStack: List<String> = emptyList(),

    @SerialName("created_at")
    val createdAt: String? = null,

    val likes: Int = 0,

    @SerialName("comment_count")
    val commentCount: Int = 0,

    @SerialName("is_featured")
    val isFeatured: Boolean = false,

    // Transient: Not stored in the 'projects' table, calculated on client
    @Transient
    val isLiked: Boolean = false
)