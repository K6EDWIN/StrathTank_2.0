package com.example.strathtankalumni.data

import java.util.UUID
import com.google.firebase.firestore.PropertyName

data class ExperienceItem(
    val id: String = UUID.randomUUID().toString(), // Auto generate a unique ID
    val companyName: String = "",
    val role: String = "",
    val startDate: String = "", //
    val endDate: String = "",
    @get:PropertyName("current")
    val isCurrent: Boolean = false
) {
    constructor() : this("", "", "", "", "", false)
}