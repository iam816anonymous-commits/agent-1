package com.skillbuilder.core.learning

import com.skillbuilder.core.domain.contracts.AgentEventListener
import com.skillbuilder.core.domain.model.*
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class LearningEngineTest {

    private lateinit var knowledgeRepository: InMemoryKnowledgeRepository
    private lateinit var skillRepository: InMemorySkillRepository
    private val events = mutableListOf<AgentEvent>()

    private val eventListener = object : AgentEventListener {
        override fun onEvent(event: AgentEvent) {
            events.add(event)
        }
    }

    @Before
    fun setUp() {
        knowledgeRepository = InMemoryKnowledgeRepository()
        skillRepository = InMemorySkillRepository()
        events.clear()
    }

    @Test
    fun testFullAutonomousLearningLoop() {
        val samplePassage = DocumentPassage(
            id = "p1",
            documentId = "doc1",
            sectionOrPage = "Section 1",
            text = "- Overview of HashMap: key value mapping\n- Hashing: derives bucket index\n- Collision Handling: uses linked lists\n- equals() and hashCode(): contract requirement",
            provenance = SourceProvenance(SourceType.REFERENCE, "doc1", "doc1.txt")
        )
        knowledgeRepository.savePassages(listOf(samplePassage))

        val goal = LearningGoal(id = "g1", goal = "Learn Java HashMap")
        val controller = AgentController(
            knowledgeRepository = knowledgeRepository,
            skillRepository = skillRepository,
            eventListener = eventListener
        )

        val skillCard = controller.executeLearningSession(goal)

        assertNotNull(skillCard)
        assertEquals("Learn Java HashMap", skillCard.skillName)
        assertTrue(skillCard.scoreBreakdown.overallScore > 0f)

        val stateEvents = events.filterIsInstance<AgentEvent.StateChanged>()
        assertTrue(stateEvents.any { it.newState == AgentState.BUILDING_SKILL_MODEL })
        assertTrue(stateEvents.any { it.newState == AgentState.GENERATING_PRACTICE })
        assertTrue(stateEvents.any { it.newState == AgentState.EVALUATING })
        assertTrue(stateEvents.any { it.newState == AgentState.COMPLETED })
    }
}
