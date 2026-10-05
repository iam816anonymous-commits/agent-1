package com.skillbuilder.core.domain.model

data class LearningGoal(
    val id: String,
    val goal: String,
    val requiredLevel: String = "practical",
    val constraints: List<String> = listOf("Use supplied references as primary source")
)
