package com.example.strathtankalumni.data

import java.util.UUID

data class ExperienceItem(
    val id: String = UUID.randomUUID().toString(), // Auto-generate a unique ID
    val companyName: String = "",
    val role: String = "",
    val startDate: String = "", // e.g., "Jan 2020"
    val endDate: String = "",   // e.g., "Dec 2022" or "Present"
    val isCurrent: Boolean = false
) {
    // Add a no-argument constructor for Firebase
    constructor() : this("", "", "", "", "", false)
}