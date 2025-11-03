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
    @ServerTimestamp
    val createdAt: Date? = null
)
