package com.skillbuilder.core.learning

import com.skillbuilder.core.domain.contracts.*
import com.skillbuilder.core.domain.model.*
import java.util.UUID

class AgentController(
    private val knowledgeRepository: KnowledgeRepository,
    private val skillRepository: SkillRepository,
    private val practiceGenerator: PracticeGenerator = DefaultPracticeGenerator(),
    private val practiceEvaluator: PracticeEvaluator = StructuredEvaluator(),
    private val reasoningProvider: ReasoningProvider? = null,
    private val webResearchProvider: WebResearchProvider? = null,
    private val policy: LearningPolicy = LearningPolicy(),
    private val eventListener: AgentEventListener? = null
) : LearningEngine {

    var currentState: AgentState = AgentState.IDLE
        private set

    private fun transitionTo(newState: AgentState) {
        val oldState = currentState
        currentState = newState
        eventListener?.onEvent(AgentEvent.StateChanged(oldState, newState))
    }

    private fun log(message: String) {
        eventListener?.onEvent(AgentEvent.LogMessage(message))
    }

    override fun executeLearningSession(goal: LearningGoal): SkillCard {
        transitionTo(AgentState.INITIALIZING)
        eventListener?.onEvent(AgentEvent.GoalReceived(goal))

        transitionTo(AgentState.LOADING_REFERENCES)
        val passages = knowledgeRepository.getAllPassages()
        eventListener?.onEvent(AgentEvent.PassageExtracted(passages.size))

        if (passages.isEmpty()) {
            transitionTo(AgentState.FAILED)
            eventListener?.onEvent(AgentEvent.LearningFailed("No reference passages found"))
            throw IllegalStateException("No reference passages available to study")
        }

        transitionTo(AgentState.ANALYZING_GOAL)
        log("Analyzing goal: ${goal.goal}")

        transitionTo(AgentState.BUILDING_SKILL_MODEL)
        var skillModel = buildInitialSkillModel(goal, passages)
        knowledgeRepository.saveSkillModel(skillModel)
        eventListener?.onEvent(AgentEvent.SkillModelCreated(skillModel.skillName, skillModel.concepts.size))

        var currentIteration = 0
        var lastEvaluation: EvaluationResult? = null
        var gaps = mutableListOf<KnowledgeGap>()

        while (currentIteration < policy.maxLearningIterations) {
            currentIteration++
            log("Starting iteration $currentIteration of ${policy.maxLearningIterations}")

            transitionTo(AgentState.GENERATING_PRACTICE)
            val task = practiceGenerator.generateTask(goal, skillModel, gaps)
            eventListener?.onEvent(AgentEvent.PracticeGenerated(task.id, task.objective))

            transitionTo(AgentState.ATTEMPTING)
            val attempt = practiceGenerator.generateAttempt(task, skillModel)

            transitionTo(AgentState.EVALUATING)
            val evaluation = practiceEvaluator.evaluateAttempt(task, attempt, skillModel, passages)
            lastEvaluation = evaluation
            eventListener?.onEvent(AgentEvent.PracticeEvaluated(task.id, evaluation.overallScore, evaluation.detectedGaps.size))

            transitionTo(AgentState.IDENTIFYING_GAPS)
            gaps = evaluation.detectedGaps.map { gapConcept ->
                KnowledgeGap(
                    id = UUID.randomUUID().toString(),
                    conceptOrTopic = gapConcept,
                    description = "Weak or missing concept identified during practice evaluation"
                )
            }.toMutableList()

            gaps.forEach { eventListener?.onEvent(AgentEvent.KnowledgeGapDetected(it)) }
            knowledgeRepository.saveKnowledgeGaps(gaps)

            if (gaps.isEmpty() || evaluation.overallScore >= policy.targetPracticalScore) {
                log("Target score or zero gaps achieved at iteration $currentIteration")
                break
            }

            if (currentIteration < policy.maxLearningIterations) {
                transitionTo(AgentState.TARGETED_LEARNING)
                skillModel = performTargetedLearning(skillModel, gaps)
                knowledgeRepository.saveSkillModel(skillModel)
                eventListener?.onEvent(AgentEvent.SkillModelUpdated(skillModel.concepts.size))

                transitionTo(AgentState.RETESTING)
                eventListener?.onEvent(AgentEvent.RetestStarted(currentIteration + 1))
            }
        }

        transitionTo(AgentState.VERIFYING)
        val finalCard = calculateAndVerifySkillCard(goal, skillModel, lastEvaluation, passages)
        skillRepository.saveSkillCard(finalCard)
        eventListener?.onEvent(AgentEvent.SkillVerified(finalCard.skillName, finalCard.status, finalCard.scoreBreakdown.overallScore))

        transitionTo(AgentState.COMPLETED)
        return finalCard
    }

    private fun buildInitialSkillModel(goal: LearningGoal, passages: List<DocumentPassage>): SkillModel {
        val concepts = mutableListOf<SkillConcept>()
        val rules = mutableListOf<SkillRule>()

        passages.forEach { passage ->
            val lines = passage.text.split("\n")
            lines.forEach { line ->
                val cleanLine = line.trim()
                if (cleanLine.startsWith("- ") || cleanLine.startsWith("* ") || cleanLine.contains(":")) {
                    val parts = cleanLine.trim('-', '*', ' ').split(":", limit = 2)
                    val conceptName = parts[0].trim()
                    if (conceptName.length in 3..40 && !concepts.any { it.name.equals(conceptName, ignoreCase = true) }) {
                        concepts.add(
                            SkillConcept(
                                id = UUID.randomUUID().toString(),
                                name = conceptName,
                                description = parts.getOrNull(1)?.trim() ?: conceptName,
                                provenance = passage.provenance
                            )
                        )
                    }
                }
            }
        }

        if (concepts.isEmpty()) {
            concepts.add(
                SkillConcept(
                    id = UUID.randomUUID().toString(),
                    name = "Core Operations",
                    description = "Fundamental operations for target skill",
                    provenance = passages.first().provenance
                )
            )
        }

        return SkillModel(
            id = UUID.randomUUID().toString(),
            skillName = goal.goal,
            prerequisites = listOf("Basic Programming Fundamentals"),
            concepts = concepts,
            rules = rules,
            patterns = listOf("Standard usage pattern"),
            commonMistakes = listOf("Misunderstanding API contracts", "Invalid configuration"),
            practicalAbilities = concepts.map { "Ability to use ${it.name}" },
            coverageScore = 0.8f
        )
    }

    private fun performTargetedLearning(
        currentModel: SkillModel,
        gaps: List<KnowledgeGap>
    ): SkillModel {
        val updatedConcepts = currentModel.concepts.toMutableList()

        gaps.forEach { gap ->
            val matchingPassages = knowledgeRepository.searchPassages(gap.conceptOrTopic)
            eventListener?.onEvent(AgentEvent.ReferenceSearched(gap.conceptOrTopic, matchingPassages.size))

            if (matchingPassages.isNotEmpty()) {
                val refPassage = matchingPassages.first()
                log("Gap '${gap.conceptOrTopic}' satisfied via local reference passage from ${refPassage.provenance.title}")
                if (!updatedConcepts.any { it.name.equals(gap.conceptOrTopic, ignoreCase = true) }) {
                    updatedConcepts.add(
                        SkillConcept(
                            id = UUID.randomUUID().toString(),
                            name = gap.conceptOrTopic,
                            description = refPassage.text.take(100),
                            provenance = refPassage.provenance
                        )
                    )
                }
            } else {
                if (reasoningProvider != null && reasoningProvider.isAvailable()) {
                    eventListener?.onEvent(AgentEvent.ExternalReasoningRequested(reasoningProvider.providerName, gap.conceptOrTopic))
                    val reasoningResponse = reasoningProvider.requestReasoning(
                        object : ReasoningRequest {
                            override val purpose = "Targeted gap learning"
                            override val query = "Explain concept: ${gap.conceptOrTopic}"
                            override val contextPassages = emptyList<DocumentPassage>()
                        }
                    )
                    eventListener?.onEvent(AgentEvent.ExternalReasoningCompleted(reasoningProvider.providerName, true))
                    updatedConcepts.add(
                        SkillConcept(
                            id = UUID.randomUUID().toString(),
                            name = gap.conceptOrTopic,
                            description = reasoningResponse.explanation,
                            provenance = reasoningResponse.provenance
                        )
                    )
                }
            }
        }

        return currentModel.copy(
            concepts = updatedConcepts,
            coverageScore = (updatedConcepts.size.toFloat() / (updatedConcepts.size + gaps.size)).coerceIn(0.5f, 1.0f)
        )
    }

    private fun calculateAndVerifySkillCard(
        goal: LearningGoal,
        skillModel: SkillModel,
        evaluation: EvaluationResult?,
        passages: List<DocumentPassage>
    ): SkillCard {
        val practicalScore = evaluation?.overallScore ?: 0.0f
        val knowledgeCoverage = skillModel.coverageScore
        val conceptualUnderstanding = (practicalScore + knowledgeCoverage) / 2f
        val sourceConfidence = 0.95f

        val overall = (
            policy.knowledgeCoverageWeight * knowledgeCoverage +
            policy.practicalPerformanceWeight * practicalScore +
            policy.conceptualUnderstandingWeight * conceptualUnderstanding +
            policy.sourceConfidenceWeight * sourceConfidence
        )

        val isLearned = overall >= policy.targetOverallScore &&
                practicalScore >= policy.targetPracticalScore &&
                (evaluation?.detectedGaps?.isEmpty() ?: false)

        val status = if (isLearned) SkillStatus.LEARNED else SkillStatus.PARTIALLY_LEARNED

        return SkillCard(
            id = UUID.randomUUID().toString(),
            skillName = goal.goal,
            status = status,
            scoreBreakdown = SkillScoreBreakdown(
                knowledgeCoverage = knowledgeCoverage,
                practicalPerformance = practicalScore,
                conceptualUnderstanding = conceptualUnderstanding,
                sourceConfidence = sourceConfidence,
                overallScore = overall
            ),
            masteredConcepts = evaluation?.strengths ?: emptyList(),
            weakAreas = evaluation?.weaknesses ?: emptyList(),
            prerequisites = skillModel.prerequisites,
            referenceSources = passages.map { it.provenance }.distinctBy { it.title },
            externalSources = emptyList()
        )
    }
}

interface LearningEngine {
    fun executeLearningSession(goal: LearningGoal): SkillCard
}
