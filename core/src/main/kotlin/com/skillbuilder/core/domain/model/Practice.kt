package com.skillbuilder.core.domain.model

enum class PracticeDifficulty {
    EASY, MEDIUM, HARD
}

data class PracticeTask(
    val id: String,
    val objective: String,
    val requirements: List<String>,
    val expectedBehavior: String,
    val evaluationCriteria: List<String>,
    val difficulty: PracticeDifficulty,
    val targetedConcepts: List<String>
)

data class PracticeAttempt(
    val id: String,
    val taskId: String,
    val solutionCodeOrAnswer: String,
    val timestamp: Long = System.currentTimeMillis()
)

enum class ConceptResultStatus {
    PASS, PARTIAL, FAIL
}

data class ConceptEvaluation(
    val conceptName: String,
    val status: ConceptResultStatus,
    val notes: String
)

enum class EvaluationType {
    STRUCTURAL_EVALUATION,
    EXECUTED_TEST
}

data class EvaluationResult(
    val id: String,
    val attemptId: String,
    val evaluationType: EvaluationType = EvaluationType.STRUCTURAL_EVALUATION,
    val overallScore: Float,
    val conceptEvaluations: List<ConceptEvaluation>,
    val strengths: List<String>,
    val weaknesses: List<String>,
    val detectedGaps: List<String>,
    val evidence: List<String>,
    val sourceReferences: List<SourceProvenance>
)
