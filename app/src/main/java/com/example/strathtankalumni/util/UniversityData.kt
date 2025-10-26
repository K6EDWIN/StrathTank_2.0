package com.example.strathtankalumni.util

object UniversityData {
    val countries = listOf("Kenya", "Uganda", "Tanzania", "Rwanda", "Nigeria", "South Africa", "Other")

    val sampleDegrees = listOf(
        "Bachelor of Informatics and Computer Science",
        "Bachelor of Commerce",
        "Bachelor of Laws",
        "Master of Science in Information Technology",
        "PhD in Computer Science"
    )

    private val countryUniversityMap = mapOf(
        "Kenya" to listOf(
            "Strathmore University",
            "University of Nairobi",
            "Kenyatta University",
            "Moi University",
            "Egerton University"
        ),
        "Uganda" to listOf(
            "Makerere University",
            "Kyambogo University",
            "Uganda Christian University"
        ),
        "Tanzania" to listOf(
            "University of Dar es Salaam",
            "Sokoine University of Agriculture"
        ),
        "Rwanda" to listOf(
            "University of Rwanda",
            "Kigali Independent University"
        ),
        "Nigeria" to listOf(
            "University of Ibadan",
            "University of Lagos",
            "Ahmadu Bello University"
        ),
        "South Africa" to listOf(
            "University of Cape Town",
            "University of the Witwatersrand",
            "Stellenbosch University"
        )
    )

    fun getUniversitiesForCountry(country: String): List<String> {
        return countryUniversityMap[country] ?: emptyList()
    }
}
