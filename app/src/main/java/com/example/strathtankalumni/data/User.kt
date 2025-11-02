package com.example.strathtankalumni.data

data class User(
    val userId: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val profilePhotoUrl: String = "",
    val email: String = "",
    val country: String = "",
    val universityName: String = "",
    val degree: String = "",
    val graduationYear: String = "",
    val role: String = "alumni",
    val about: String = "",
    val experience: String = "",
    val skills: List<String> = emptyList(),
    val linkedinUrl: String = ""

) {
    fun toMap(): Map<String, Any> {
        return mapOf(
            "userId" to userId,
            "firstName" to firstName,
            "lastName" to lastName,
            "email" to email,
            "country" to country,
            "universityName" to universityName,
            "degree" to degree,
            "graduationYear" to graduationYear,
            "role" to role,
            "about" to about,
            "experience" to experience,
            "skills" to skills,
            "profilePhotoUrl" to profilePhotoUrl,
            "linkedinUrl" to linkedinUrl
        )
    }
}
