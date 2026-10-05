package com.skillbuilder.core.learning

import com.skillbuilder.core.domain.contracts.KnowledgeRepository
import com.skillbuilder.core.domain.contracts.PracticeEvaluator
import com.skillbuilder.core.domain.contracts.PracticeGenerator
import com.skillbuilder.core.domain.model.*
import java.util.UUID

class InMemoryKnowledgeRepository : KnowledgeRepository {
    private val documents = mutableMapOf<String, Document>()
    private val passages = mutableListOf<DocumentPassage>()
    private var skillModel: SkillModel? = null
    private val gaps = mutableListOf<KnowledgeGap>()
    private val conflicts = mutableListOf<KnowledgeConflict>()

    override fun saveDocument(document: Document) {
        documents[document.id] = document
    }

    override fun savePassages(passages: List<DocumentPassage>) {
        this.passages.addAll(passages)
    }

    override fun getPassagesForDocument(documentId: String): List<DocumentPassage> {
        return passages.filter { it.documentId == documentId }
    }

    override fun searchPassages(query: String): List<DocumentPassage> {
        val keywords = query.lowercase().split(" ", ",", ";", "/").filter { it.length > 2 }
        if (keywords.isEmpty()) return passages
        return passages.filter { passage ->
            val textLower = passage.text.lowercase()
            keywords.any { textLower.contains(it) }
        }
    }

    override fun getAllPassages(): List<DocumentPassage> {
        return passages
    }

    override fun saveSkillModel(skillModel: SkillModel) {
        this.skillModel = skillModel
    }

    override fun getSkillModel(skillId: String): SkillModel? {
        return skillModel
    }

    override fun saveKnowledgeGaps(gaps: List<KnowledgeGap>) {
        this.gaps.clear()
        this.gaps.addAll(gaps)
    }

    override fun getKnowledgeGaps(): List<KnowledgeGap> {
        return gaps
    }

    override fun saveConflict(conflict: KnowledgeConflict) {
        conflicts.add(conflict)
    }

    override fun getConflicts(): List<KnowledgeConflict> {
        return conflicts
    }
}

class DefaultPracticeGenerator : PracticeGenerator {
    override fun generateTask(
        goal: LearningGoal,
        skillModel: SkillModel,
        targetGaps: List<KnowledgeGap>
    ): PracticeTask {
        val targetedConcepts = if (targetGaps.isNotEmpty()) {
            targetGaps.map { it.conceptOrTopic }
        } else {
            skillModel.concepts.map { it.name }.take(3)
        }

        return PracticeTask(
            id = UUID.randomUUID().toString(),
            objective = "Demonstrate core usage for ${skillModel.skillName}: ${goal.goal}",
            requirements = listOf(
                "Must address ${targetedConcepts.joinToString(", ")}",
                "Follow standard implementation patterns",
                "Avoid common pitfall mistakes: ${skillModel.commonMistakes.take(2).joinToString("; ")}"
            ),
            expectedBehavior = "Correct structural code or solution satisfying operations",
            evaluationCriteria = listOf("Creation", "Lookup/Operation", "Contract compliance"),
            difficulty = if (targetGaps.isNotEmpty()) PracticeDifficulty.MEDIUM else PracticeDifficulty.EASY,
            targetedConcepts = targetedConcepts
        )
    }

    override fun generateAttempt(task: PracticeTask, skillModel: SkillModel): PracticeAttempt {
        val codeBuilder = StringBuilder()
        codeBuilder.append("// Solution attempting ${task.objective}\n")

        // Include all concepts currently in skill model
        // (On initial iteration if gaps exist in references, initial model won't have them until targeted learning runs)
        skillModel.concepts.forEach { concept ->
            codeBuilder.append("// Concept implemented: ${concept.name}\n")
            codeBuilder.append("// ${concept.description}\n")
        }

        codeBuilder.append("public class PracticalSolution {\n")
        codeBuilder.append("    // Implementation covering ${skillModel.concepts.joinToString { it.name }}\n")
        codeBuilder.append("}\n")

        return PracticeAttempt(
            id = UUID.randomUUID().toString(),
            taskId = task.id,
            solutionCodeOrAnswer = codeBuilder.toString()
        )
    }
}

class StructuredEvaluator : PracticeEvaluator {
    override fun evaluateAttempt(
        task: PracticeTask,
        attempt: PracticeAttempt,
        skillModel: SkillModel,
        passages: List<DocumentPassage>
    ): EvaluationResult {
        val solutionText = attempt.solutionCodeOrAnswer.lowercase()
        val conceptEvals = mutableListOf<ConceptEvaluation>()
        val strengths = mutableListOf<String>()
        val weaknesses = mutableListOf<String>()
        val detectedGaps = mutableListOf<String>()

        var totalPassed = 0

        // Extract key topics required by references
        val allRequiredTopics = passages.flatMap { passage ->
            passage.text.split("\n")
                .map { line -> line.trim() }
                .filter { line -> line.startsWith("- ") || line.startsWith("* ") || line.contains(":") }
                .map { line -> line.trim('-', '*', ' ').split(":", limit = 2)[0].trim() }
                .filter { topic -> topic.length in 3..40 }
        }.distinct()

        val evaluationTargetTopics = if (allRequiredTopics.isNotEmpty()) allRequiredTopics else skillModel.concepts.map { it.name }
        val totalConcepts = evaluationTargetTopics.size.coerceAtLeast(1)

        evaluationTargetTopics.forEach { topic ->
            val keywords = topic.lowercase().split(" ", "/")
            val matches = keywords.any { solutionText.contains(it) }

            if (matches) {
                totalPassed++
                conceptEvals.add(
                    ConceptEvaluation(
                        conceptName = topic,
                        status = ConceptResultStatus.PASS,
                        notes = "Solution correctly references and satisfies $topic"
                    )
                )
                strengths.add("Demonstrates $topic")
            } else {
                conceptEvals.add(
                    ConceptEvaluation(
                        conceptName = topic,
                        status = ConceptResultStatus.FAIL,
                        notes = "Missing required concept or operation: $topic"
                    )
                )
                weaknesses.add("Lacks clear usage of $topic")
                detectedGaps.add(topic)
            }
        }

        val rawScore = totalPassed.toFloat() / totalConcepts
        val evidenceList = passages.take(2).map { "Reference context: ${it.sectionOrPage}" }

        return EvaluationResult(
            id = UUID.randomUUID().toString(),
            attemptId = attempt.id,
            evaluationType = EvaluationType.STRUCTURAL_EVALUATION,
            overallScore = rawScore,
            conceptEvaluations = conceptEvals,
            strengths = strengths,
            weaknesses = weaknesses,
            detectedGaps = detectedGaps,
            evidence = evidenceList,
            sourceReferences = passages.take(2).map { it.provenance }
        )
    }
}
