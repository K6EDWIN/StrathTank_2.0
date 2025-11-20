package com.example.strathtankalumni.data

import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.PropertyName
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

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
    val mediaImageUrls: List<String> = emptyList(),
    val pdfUrl: String = "",
    val categories: List<String> = emptyList(),
    val programmingLanguages: List<String> = emptyList(),
    val databaseUsed: List<String> = emptyList(),
    val techStack: List<String> = emptyList(),
    @ServerTimestamp
    val createdAt: Date? = null,
    val likes: Int = 0,
    val commentCount: Int = 0,
    val isFeatured: Boolean = false,
    @get:PropertyName("liked")
    val isLiked: Boolean = false
)