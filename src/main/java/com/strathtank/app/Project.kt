package com.strathtank.app

import java.io.Serializable
import java.util.*

data class Project(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String,
    val link: String? = null,
    val fileUrls: List<String> = emptyList(),
    val authorId: String,
    val authorName: String,
    val createdAt: Date = Date(),
    val updatedAt: Date = Date(),
    val status: ProjectStatus = ProjectStatus.PENDING,
    val tags: List<String> = emptyList(),
    val category: ProjectCategory = ProjectCategory.OTHER,
    val likes: Int = 0,
    val views: Int = 0
) : Serializable

enum class ProjectStatus {
    PENDING,
    APPROVED,
    REJECTED
}

enum class ProjectCategory {
    WEB_DEVELOPMENT,
    MOBILE_DEVELOPMENT,
    DATA_SCIENCE,
    MACHINE_LEARNING,
    GAME_DEVELOPMENT,
    DESIGN,
    RESEARCH,
    OTHER
}
