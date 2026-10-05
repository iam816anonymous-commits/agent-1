package com.skillbuilder.core.domain.model

data class LearningPolicy(
    val maxLearningIterations: Int = 3,
    val maxPracticeAttemptsPerIteration: Int = 2,
    val maxExternalAiRequests: Int = 3,
    val maxWebResearchRequests: Int = 5,
    val targetOverallScore: Float = 0.85f,
    val targetPracticalScore: Float = 0.80f,
    val knowledgeCoverageWeight: Float = 0.30f,
    val practicalPerformanceWeight: Float = 0.30f,
    val conceptualUnderstandingWeight: Float = 0.20f,
    val sourceConfidenceWeight: Float = 0.20f
)
