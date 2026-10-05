# Skill Builder Agent

An autonomous, offline-first Android learning agent orchestrator designed to study reference documents, construct domain-independent skill models, generate practical exercises, evaluate attempts, identify knowledge gaps, perform targeted study, and store verified skill records.

---

## Architecture Overview

```
                      Skill Builder Architecture
                                   │
              ┌────────────────────┼────────────────────┐
              │                    │                    │
         :core                :infrastructure          :app
   (Domain & Engine)         (Document & External)   (Compose UI)
              │                    │                    │
              └────────────────────┼────────────────────┘
                                   │
                            Agent Core API
```

The project is structured into three distinct modules:

1. **`:core`**: Pure Kotlin/JVM module containing no Android framework dependencies. Holds domain models (`LearningGoal`, `SkillModel`, `PracticeTask`, `EvaluationResult`, `KnowledgeGap`, `SourceProvenance`, `SkillCard`), state machine (`AgentController`), `StructuredEvaluator`, and contract interfaces.
2. **`:infrastructure`**: Implements document readers (TXT, Markdown, PDF), reference file processing, mock/API reasoning providers (`MockReasoningProvider`, `ApiReasoningProvider`), and web research providers (`MockWebResearchProvider`).
3. **`:app`**: Android application host built with Jetpack Compose and Room DB (`RoomSkillRepository`) displaying live state machine events, gap detection feeds, and final skill card source provenance.

---

## Autonomous Learning Loop

```
REFERENCE DOCUMENTS ──► SKILL MODEL ──► PRACTICE TASK ──► ATTEMPT ──► EVALUATE
                                                                            │
                                                                            ▼
SKILL CARD persistance ◄── VERIFY ◄── RETEST ◄── TARGETED STUDY ◄── DETECT GAP
```

1. **`INITIALIZING` / `LOADING_REFERENCES`**: Local reference documents (PDF, MD, TXT) are read and extracted into passages.
2. **`BUILDING_SKILL_MODEL`**: Initial domain-independent concepts, rules, patterns, and practical abilities are structured.
3. **`GENERATING_PRACTICE` & `ATTEMPTING`**: Scenario-based tasks and practical solutions are formulated.
4. **`EVALUATING` & `IDENTIFYING_GAPS`**: Solution is evaluated against required concepts using `StructuredEvaluator`. Any missing or weak concepts trigger `KnowledgeGap` detection.
5. **`TARGETED_LEARNING` & `RETESTING`**: Local reference passages are searched first to close the gap. If insufficient, optional external reasoning providers (`ChatGPT`, `Gemini`) are queried.
6. **`VERIFYING` & `COMPLETED`**: Overall evidence-based skill confidence score is calculated:
   $$\text{Overall} = 0.30 \times \text{Coverage} + 0.30 \times \text{Practical} + 0.20 \times \text{Conceptual} + 0.20 \times \text{SourceConfidence}$$
   If threshold ($\ge 85\%$) is met, status is set to `LEARNED` and persisted as a `SkillCard`.

---

## Source Provenance Hierarchy

Every knowledge item strictly records its provenance:
1. **`LEVEL 1 (REFERENCE)`**: User-provided reference documents.
2. **`LEVEL 2 (OFFICIAL_WEB)`**: Official web documentation.
3. **`LEVEL 3 (CHATGPT / GEMINI)`**: External reasoning providers.
4. **`LEVEL 4 (INFERENCE)`**: Agent inference.

---

## Building and Running Tests Locally

### Run Unit and Integration Tests
```bash
gradle :core:test :infrastructure:test
```

### Build Debug APK
```bash
gradle :app:assembleDebug
```
Output APK location: `app/build/outputs/apk/debug/app-debug.apk`

---

## Build APK from GitHub Actions

1. Push your changes or open a pull request to `main` / `master`.
2. Open the GitHub repository and navigate to the **Actions** tab.
3. Select the **Android CI & APK Build** workflow run.
4. Once the workflow completes successfully, scroll down to **Artifacts**.
5. Download `skill-builder-debug-apk`, extract the zip file, and install `app-debug.apk` onto an Android device or emulator.
