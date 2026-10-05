package com.skillbuilder.core.domain.model

data class SkillConcept(
    val id: String,
    val name: String,
    val description: String,
    val provenance: SourceProvenance
)

data class SkillRule(
    val id: String,
    val rule: String,
    val provenance: SourceProvenance
)

data class SkillModel(
    val id: String,
    val skillName: String,
    val prerequisites: List<String>,
    val concepts: List<SkillConcept>,
    val rules: List<SkillRule>,
    val patterns: List<String>,
    val commonMistakes: List<String>,
    val practicalAbilities: List<String>,
    val coverageScore: Float = 0.0f
)
