package com.skillbuilder.core.domain.model

enum class AgentState {
    IDLE,
    INITIALIZING,
    LOADING_REFERENCES,
    ANALYZING_GOAL,
    BUILDING_SKILL_MODEL,
    GENERATING_PRACTICE,
    ATTEMPTING,
    EVALUATING,
    IDENTIFYING_GAPS,
    TARGETED_LEARNING,
    RETESTING,
    VERIFYING,
    COMPLETED,
    FAILED,
    PAUSED
}

sealed class AgentEvent {
    data class StateChanged(val previousState: AgentState, val newState: AgentState) : AgentEvent()
    data class GoalReceived(val goal: LearningGoal) : AgentEvent()
    data class ReferencesLoaded(val documentCount: Int) : AgentEvent()
    data class PassageExtracted(val passageCount: Int) : AgentEvent()
    data class SkillModelCreated(val skillName: String, val conceptCount: Int) : AgentEvent()
    data class PracticeGenerated(val taskId: String, val objective: String) : AgentEvent()
    data class PracticeEvaluated(val taskId: String, val score: Float, val gapCount: Int) : AgentEvent()
    data class KnowledgeGapDetected(val gap: KnowledgeGap) : AgentEvent()
    data class ReferenceSearched(val query: String, val matchCount: Int) : AgentEvent()
    data class ExternalReasoningRequested(val providerName: String, val purpose: String) : AgentEvent()
    data class ExternalReasoningCompleted(val providerName: String, val success: Boolean) : AgentEvent()
    data class WebResearchRequested(val query: String) : AgentEvent()
    data class WebResearchCompleted(val query: String, val success: Boolean) : AgentEvent()
    data class SkillModelUpdated(val newConceptCount: Int) : AgentEvent()
    data class RetestStarted(val iteration: Int) : AgentEvent()
    data class SkillVerified(val skillName: String, val status: SkillStatus, val score: Float) : AgentEvent()
    data class LogMessage(val message: String, val timestamp: Long = System.currentTimeMillis()) : AgentEvent()
    data class LearningFailed(val reason: String) : AgentEvent()
}
