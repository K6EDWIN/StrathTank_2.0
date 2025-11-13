package com.example.strathtankalumni.data

import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

// -------------------------
// Data class for a Comment
// -------------------------
data class Comment(
    @get:Exclude var id: String = "",
    val projectId: String = "", // Link to the project
    val userId: String = "",
    val userName: String = "", // Display name for comment list
    val userPhotoUrl: String? = null,
    val text: String = "",
    val parentCommentId: String? = null, // For replies (if implemented later)
    val likes: Int = 0,
    @ServerTimestamp
    val createdAt: Date? = null,
    // Add isLiked state for UI tracking (client-side)
    val isLiked: Boolean = false
)

// -------------------------------------
// Data class for a Project Like
// This document's ID will be projectId_userId
// -------------------------------------
data class ProjectLike(
    val userId: String = "",
    val projectId: String = "",
    @ServerTimestamp
    val createdAt: Date? = null
)

// -------------------------------------
// Main Project data class
// -------------------------------------
data class Project(
    @get:Exclude var id: String = "",
    val userId: String = "",
    val title: String = "",
    val description: String = "",
    val projectUrl: String = "",
    val githubUrl: String = "",
    val projectType: String = "",
    val imageUrl: String = "",
    // NEW FIELDS for Media
    val mediaImageUrls: List<String> = emptyList(), // Multiple gallery images
    val pdfUrl: String = "", // Documentation PDF
    // END NEW FIELDS for Media
    val categories: List<String> = emptyList(),
    // NEW FIELDS for Tech Stack
    val programmingLanguages: List<String> = emptyList(),
    val databaseUsed: List<String> = emptyList(),
    val techStack: List<String> = emptyList(),
    // END NEW FIELDS
    @ServerTimestamp
    val createdAt: Date? = null,
    // UI display fields (counts)
    val likes: Int = 0,
    val commentCount: Int = 0, // Used to update the main project list/detail
    // Add isLiked state for UI tracking (client-side)
    val isLiked: Boolean = false
)