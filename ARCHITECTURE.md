# Architecture & Core Reusability Guide

This document explains the architecture of the **Skill Builder Agent** engine and details how external JVM/Kotlin applications (CLI tools, server backends, desktop apps, or other mobile platforms) can embed and reuse `:core`.

---

## 1. Engine Core Boundaries

The `:core` module is designed with strict boundaries:
- **Zero Android dependencies**: Uses only pure Kotlin and standard Java APIs.
- **UI & Storage Independent**: Communicates via event listeners (`AgentEventListener`) and repository contracts (`KnowledgeRepository`, `SkillRepository`).
- **Domain Independent**: Contains no hardcoded technology rules (e.g. no `if (skill == "HashMap")`). All domain knowledge is ingested from reference documents.

```
+-------------------------------------------------------------------+
|                        HOST APPLICATION                           |
|            (Android App / CLI Tool / Backend Service)             |
+-------------------------------------------------------------------+
                                  |
                                  v
+-------------------------------------------------------------------+
|                       SKILL BUILDER CORE                          |
|  - AgentController (State Machine)                                |
|  - StructuredEvaluator                                            |
|  - DefaultPracticeGenerator                                       |
|  - Domain Models & LearningPolicy                                 |
+-------------------------------------------------------------------+
       |                          |                         |
       v                          v                         v
[DocumentReader]          [ReasoningProvider]      [KnowledgeRepository]
```

---

## 2. Reusability Interfaces

To embed `:core` in another project, implement or inject the following contracts:

### A. Document Reading & Storage
- **`DocumentReader`**: Implement to support custom file types or text extractors.
- **`KnowledgeRepository`**: Storage contract for passages, concepts, and knowledge gaps (`InMemoryKnowledgeRepository` or DB backed).
- **`SkillRepository`**: Storage contract for generated `SkillCard` instances (`InMemorySkillRepository` or Room/SQLite backed).

### B. Reasoning & Research Providers
- **`ReasoningProvider`**: Plug in LLM gateways (ChatGPT, Gemini, Ollama, Claude) or use `MockReasoningProvider`.
- **`WebResearchProvider`**: Plug in custom web search engines or use `MockWebResearchProvider`.

---

## 3. Kotlin Embedding Example

```kotlin
import com.skillbuilder.core.domain.model.*
import com.skillbuilder.core.learning.*

fun main() {
    // 1. Prepare Repositories
    val knowledgeRepo = InMemoryKnowledgeRepository()
    val skillRepo = InMemorySkillRepository()

    // 2. Instantiate Agent Controller
    val controller = AgentController(
        knowledgeRepository = knowledgeRepo,
        skillRepository = skillRepo,
        eventListener = object : AgentEventListener {
            override fun onEvent(event: AgentEvent) {
                println("Event: $event")
            }
        }
    )

    // 3. Execute Autonomous Learning Session
    val goal = LearningGoal(id = "g1", goal = "Learn Custom Topic")
    val skillCard: SkillCard = controller.executeLearningSession(goal)

    println("Learned Skill: ${skillCard.skillName}, Status: ${skillCard.status}")
}
```
