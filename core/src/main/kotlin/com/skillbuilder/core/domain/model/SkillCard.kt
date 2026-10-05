package com.skillbuilder.core.domain.model

enum class SkillStatus {
    UNKNOWN,
    PARTIALLY_LEARNED,
    LEARNED
}

data class SkillScoreBreakdown(
    val knowledgeCoverage: Float,
    val practicalPerformance: Float,
    val conceptualUnderstanding: Float,
    val sourceConfidence: Float,
    val overallScore: Float
)

data class SkillCard(
    val id: String,
    val skillName: String,
    val status: SkillStatus,
    val scoreBreakdown: SkillScoreBreakdown,
    val masteredConcepts: List<String>,
    val weakAreas: List<String>,
    val prerequisites: List<String>,
    val referenceSources: List<SourceProvenance>,
    val externalSources: List<SourceProvenance>,
    val lastVerifiedTimestamp: Long = System.currentTimeMillis()
)
