package com.skillbuilder.core.learning

import com.skillbuilder.infrastructure.documents.DocumentManager
import com.skillbuilder.infrastructure.providers.MockReasoningProvider
import com.skillbuilder.infrastructure.providers.MockWebResearchProvider
import com.skillbuilder.core.domain.contracts.AgentEventListener
import com.skillbuilder.core.domain.model.*
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.io.File

class MultiSkillEndToEndTest {

    private lateinit var knowledgeRepository: InMemoryKnowledgeRepository
    private lateinit var skillRepository: InMemorySkillRepository
    private lateinit var documentManager: DocumentManager
    private val events = mutableListOf<AgentEvent>()

    private val eventListener = object : AgentEventListener {
        override fun onEvent(event: AgentEvent) {
            events.add(event)
        }
    }

    private fun getDemoFile(path: String): File {
        val f1 = File(path)
        if (f1.exists()) return f1
        val f2 = File("../$path")
        if (f2.exists()) return f2
        return f1
    }

    @Before
    fun setUp() {
        knowledgeRepository = InMemoryKnowledgeRepository()
        skillRepository = InMemorySkillRepository()
        documentManager = DocumentManager()
        events.clear()
    }

    @Test
    fun testJavaHashMapEndToEnd() {
        val mdFile = getDemoFile("demo/references/hashmap_notes.md")
        assertTrue("Reference file must exist", mdFile.exists())
        val (doc, passages) = documentManager.processDocument(
            id = "doc_hashmap_1",
            title = "HashMap Notes",
            fileName = mdFile.name,
            format = "MD",
            inputStreamProvider = { mdFile.inputStream() }
        )
        assertEquals(ExtractionStatus.SUCCESS, doc.status)
        knowledgeRepository.saveDocument(doc)
        knowledgeRepository.savePassages(passages)

        val goal = LearningGoal(id = "goal_hashmap", goal = "Learn Java HashMap well enough to use it correctly.")
        val controller = AgentController(
            knowledgeRepository = knowledgeRepository,
            skillRepository = skillRepository,
            reasoningProvider = MockReasoningProvider("MockChatGPT"),
            webResearchProvider = MockWebResearchProvider(),
            eventListener = eventListener
        )

        val skillCard = controller.executeLearningSession(goal)

        assertNotNull(skillCard)
        assertEquals("Learn Java HashMap well enough to use it correctly.", skillCard.skillName)
        assertTrue(skillCard.status == SkillStatus.LEARNED || skillCard.status == SkillStatus.PARTIALLY_LEARNED)
        assertTrue(skillCard.scoreBreakdown.overallScore >= 0.5f)
        assertTrue(skillCard.referenceSources.isNotEmpty())

        val states = events.filterIsInstance<AgentEvent.StateChanged>().map { it.newState }
        assertTrue(states.contains(AgentState.BUILDING_SKILL_MODEL))
        assertTrue(states.contains(AgentState.GENERATING_PRACTICE))
        assertTrue(states.contains(AgentState.EVALUATING))
        assertTrue(states.contains(AgentState.COMPLETED))
    }

    @Test
    fun testSpringBootRestControllersEndToEnd() {
        val mdFile = getDemoFile("demo/references/springboot_rest.md")
        assertTrue(mdFile.exists())
        val (doc, passages) = documentManager.processDocument(
            id = "doc_spring_1",
            title = "Spring Boot REST",
            fileName = mdFile.name,
            format = "MD",
            inputStreamProvider = { mdFile.inputStream() }
        )
        knowledgeRepository.saveDocument(doc)
        knowledgeRepository.savePassages(passages)

        val goal = LearningGoal(id = "goal_spring", goal = "Learn Spring Boot REST Controllers")
        val controller = AgentController(
            knowledgeRepository = knowledgeRepository,
            skillRepository = skillRepository,
            eventListener = eventListener
        )

        val skillCard = controller.executeLearningSession(goal)
        assertEquals("Learn Spring Boot REST Controllers", skillCard.skillName)
        assertTrue(skillCard.scoreBreakdown.overallScore > 0f)
    }

    @Test
    fun testGitBranchingAndMergingEndToEnd() {
        val mdFile = getDemoFile("demo/references/git_branching.md")
        assertTrue(mdFile.exists())
        val (doc, passages) = documentManager.processDocument(
            id = "doc_git_1",
            title = "Git Branching",
            fileName = mdFile.name,
            format = "MD",
            inputStreamProvider = { mdFile.inputStream() }
        )
        knowledgeRepository.saveDocument(doc)
        knowledgeRepository.savePassages(passages)

        val goal = LearningGoal(id = "goal_git", goal = "Learn Git Branching and Merging")
        val controller = AgentController(
            knowledgeRepository = knowledgeRepository,
            skillRepository = skillRepository,
            eventListener = eventListener
        )

        val skillCard = controller.executeLearningSession(goal)
        assertEquals("Learn Git Branching and Merging", skillCard.skillName)
        assertTrue(skillCard.scoreBreakdown.overallScore > 0f)
    }

    @Test
    fun testOfflineFallbackAndLimits() {
        val txtFile = getDemoFile("demo/references/java_collections.txt")
        val (doc, passages) = documentManager.processDocument(
            id = "doc_txt_1",
            title = "Java Collections",
            fileName = txtFile.name,
            format = "TXT",
            inputStreamProvider = { txtFile.inputStream() }
        )
        knowledgeRepository.saveDocument(doc)
        knowledgeRepository.savePassages(passages)

        val controller = AgentController(
            knowledgeRepository = knowledgeRepository,
            skillRepository = skillRepository,
            reasoningProvider = null,
            webResearchProvider = null,
            eventListener = eventListener
        )

        val skillCard = controller.executeLearningSession(
            LearningGoal(id = "offline_goal", goal = "Learn Java Collections Offline")
        )

        assertNotNull(skillCard)
        assertEquals(SkillStatus.LEARNED, skillCard.status)
    }
}
