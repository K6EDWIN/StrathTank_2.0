package com.example.strathtankalumni.data

import com.google.firebase.firestore.PropertyName
import java.util.UUID

data class ExperienceItem(
    val id: String = UUID.randomUUID().toString(),
    val companyName: String = "",
    val role: String = "",
    val startDate: String = "",
    val endDate: String = "",

    @get:PropertyName("current")
    val isCurrent: Boolean = false
) {
    constructor() : this("", "", "", "", "", false)
}