package com.example.strathtankalumni.data

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date


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
    @ServerTimestamp
    val requestedAt: Date? = null,
    @ServerTimestamp
    val updatedAt: Date? = null
)