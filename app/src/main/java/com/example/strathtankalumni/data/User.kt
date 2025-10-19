package com.example.strathtankalumni.data

data class User(
    val userId: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    val country: String = "",
    val universityName: String = "",
    val degree: String = "",
    val role: String = "pending"
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
            "role" to role
        )
    }
}
