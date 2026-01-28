package com.example.strathtankalumni.data

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName
import java.util.UUID

@Serializable
data class ExperienceItem(
    val id: String = UUID.randomUUID().toString(),

    @SerialName("company_name")
    val companyName: String = "",

    val role: String = "",

    @SerialName("start_date")
    val startDate: String = "",

    @SerialName("end_date")
    val endDate: String = "",

    // ✅ CHANGED: Replaced Firebase @PropertyName with @SerialName
    // Mapping "isCurrent" to "is_current" (or "current") for the JSON output
    @SerialName("is_current")
    val isCurrent: Boolean = false
) {
    // Secondary constructor is usually not needed with default values,
    // but can be kept if your codebase relies on it.
    constructor() : this("", "", "", "", "", false)
}