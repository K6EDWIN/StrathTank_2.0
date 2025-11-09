package com.example.strathtankalumni.data

import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class Project(
    @get:Exclude var id: String = "",
    val userId: String = "",
    val title: String = "",
    val description: String = "",
    val projectUrl: String = "",
    val githubUrl: String = "",
    val projectType: String = "",
    val imageUrl: String = "",
    val categories: List<String> = emptyList(),
    // NEW FIELDS for Tech Stack
    val programmingLanguages: List<String> = emptyList(),
    val databaseUsed: List<String> = emptyList(),
    val techStack: List<String> = emptyList(),
    // END NEW FIELDS
    @ServerTimestamp
    val createdAt: Date? = null,
    // Fields added for UI display (in a real app, these might come from a separate count collection/API)
    val likes: Int = 0,
    val commentCount: Int = 0,
    val isLiked: Boolean = false // Client-side state
)
