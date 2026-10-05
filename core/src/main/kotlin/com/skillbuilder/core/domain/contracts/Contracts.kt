package com.skillbuilder.core.domain.contracts

import com.skillbuilder.core.domain.model.*

interface DocumentReader {
    fun supportsFormat(format: String): Boolean
    fun extractPassages(fileName: String, inputStreamProvider: () -> java.io.InputStream): List<DocumentPassage>
}

interface KnowledgeRepository {
    fun saveDocument(document: Document)
    fun savePassages(passages: List<DocumentPassage>)
    fun getPassagesForDocument(documentId: String): List<DocumentPassage>
    fun searchPassages(query: String): List<DocumentPassage>
    fun getAllPassages(): List<DocumentPassage>
    fun saveSkillModel(skillModel: SkillModel)
    fun getSkillModel(skillId: String): SkillModel?
    fun saveKnowledgeGaps(gaps: List<KnowledgeGap>)
    fun getKnowledgeGaps(): List<KnowledgeGap>
    fun saveConflict(conflict: KnowledgeConflict)
    fun getConflicts(): List<KnowledgeConflict>
}

interface SkillRepository {
    fun saveSkillCard(skillCard: SkillCard)
    fun getSkillCard(skillId: String): SkillCard?
    fun getAllSkillCards(): List<SkillCard>
}

interface PracticeGenerator {
    fun generateTask(goal: LearningGoal, skillModel: SkillModel, targetGaps: List<KnowledgeGap>): PracticeTask
    fun generateAttempt(task: PracticeTask, skillModel: SkillModel): PracticeAttempt
}

interface PracticeEvaluator {
    fun evaluateAttempt(
        task: PracticeTask,
        attempt: PracticeAttempt,
        skillModel: SkillModel,
        passages: List<DocumentPassage>
    ): EvaluationResult
}

interface ReasoningRequest {
    val purpose: String
    val query: String
    val contextPassages: List<DocumentPassage>
}

data class ReasoningResponse(
    val providerName: String,
    val explanation: String,
    val identifiedConcepts: List<String>,
    val confidence: Float,
    val provenance: SourceProvenance
)

interface ReasoningProvider {
    val providerName: String
    fun isAvailable(): Boolean
    fun requestReasoning(request: ReasoningRequest): ReasoningResponse
}

data class WebResearchRequest(
    val query: String,
    val reason: String
)

data class WebResearchResponse(
    val url: String,
    val title: String,
    val snippet: String,
    val sourceDomain: String,
    val provenance: SourceProvenance
)

interface WebResearchProvider {
    fun isAvailable(): Boolean
    fun performResearch(request: WebResearchRequest): List<WebResearchResponse>
}

interface AgentEventListener {
    fun onEvent(event: AgentEvent)
}
