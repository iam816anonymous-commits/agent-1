package com.skillbuilder.core.domain.model

data class KnowledgeGap(
    val id: String,
    val conceptOrTopic: String,
    val description: String,
    val severity: GapSeverity = GapSeverity.MEDIUM,
    val resolved: Boolean = false
)

enum class GapSeverity {
    LOW, MEDIUM, HIGH, CRITICAL
}

data class KnowledgeConflict(
    val id: String,
    val claimA: String,
    val sourceA: SourceProvenance,
    val claimB: String,
    val sourceB: SourceProvenance,
    val resolution: String?,
    val resolutionReason: String?,
    val confidence: Float
)
