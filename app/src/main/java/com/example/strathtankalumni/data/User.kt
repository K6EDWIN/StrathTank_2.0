package com.example.strathtankalumni.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class User(
    @SerialName("user_id")
    val userId: String = "",

    @SerialName("first_name")
    val firstName: String = "",

    @SerialName("last_name")
    val lastName: String = "",

    val email: String = "",
    val role: String = "alumni",

    // ✅ FIX 1: Nullable types for optional fields
    // ✅ FIX 2: @SerialName to match DB column names (snake_case)
    @SerialName("profile_photo_url")
    val profilePhotoUrl: String? = null,

    val country: String? = null,

    @SerialName("university_name")
    val universityName: String? = null,

    val degree: String? = null,

    @SerialName("graduation_year")
    val graduationYear: String? = null,

    val about: String? = null,

    @SerialName("linkedin_url")
    val linkedinUrl: String? = null,

    // Lists usually default to empty if null in DB, but safe to keep as is if your DB returns []
    val experience: List<ExperienceItem> = emptyList(),
    val skills: List<String> = emptyList()
) {
    // You likely don't need this toMap() anymore with Supabase (it handles serialization automatically),
    // but I'll leave it here updated just in case you use it elsewhere.
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "user_id" to userId,
            "first_name" to firstName,
            "last_name" to lastName,
            "email" to email,
            "country" to country,
            "university_name" to universityName,
            "degree" to degree,
            "graduation_year" to graduationYear,
            "role" to role,
            "about" to about,
            "experience" to experience,
            "skills" to skills,
            "profile_photo_url" to profilePhotoUrl,
            "linkedin_url" to linkedinUrl
        )
    }
}