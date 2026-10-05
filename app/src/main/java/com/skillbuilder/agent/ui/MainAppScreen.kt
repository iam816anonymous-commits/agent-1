package com.skillbuilder.agent.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.skillbuilder.core.domain.contracts.AgentEventListener
import com.skillbuilder.core.domain.model.*
import com.skillbuilder.core.learning.AgentController
import com.skillbuilder.core.learning.InMemoryKnowledgeRepository
import com.skillbuilder.core.learning.InMemorySkillRepository
import com.skillbuilder.infrastructure.documents.DocumentManager
import com.skillbuilder.infrastructure.providers.MockReasoningProvider
import com.skillbuilder.infrastructure.providers.MockWebResearchProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayInputStream
import java.io.File
import java.util.UUID

sealed class Screen {
    object SkillsList : Screen()
    object StartLearning : Screen()
    data class LiveSession(val goal: LearningGoal) : Screen()
    data class SkillDetail(val card: SkillCard) : Screen()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen() {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.SkillsList) }
    val skillRepository = remember { InMemorySkillRepository() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Skill Builder Agent", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (val screen = currentScreen) {
                is Screen.SkillsList -> SkillsListScreen(
                    skillRepository = skillRepository,
                    onStartNew = { currentScreen = Screen.StartLearning },
                    onSelectSkill = { card -> currentScreen = Screen.SkillDetail(card) }
                )
                is Screen.StartLearning -> StartLearningScreen(
                    onStartSession = { goal -> currentScreen = Screen.LiveSession(goal) },
                    onCancel = { currentScreen = Screen.SkillsList }
                )
                is Screen.LiveSession -> LiveSessionScreen(
                    goal = screen.goal,
                    skillRepository = skillRepository,
                    onCompleted = { card -> currentScreen = Screen.SkillDetail(card) }
                )
                is Screen.SkillDetail -> SkillDetailScreen(
                    card = screen.card,
                    onBack = { currentScreen = Screen.SkillsList }
                )
            }
        }
    }
}

@Composable
fun SkillsListScreen(
    skillRepository: InMemorySkillRepository,
    onStartNew: () -> Unit,
    onSelectSkill: (SkillCard) -> Unit
) {
    val cards = remember { skillRepository.getAllSkillCards() }

    Box(modifier = Modifier.fillMaxSize()) {
        if (cards.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.School,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "No learned skills yet",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Start a learning session to build your first autonomous skill card.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(cards) { card ->
                    Card(
                        onClick = { onSelectSkill(card) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = card.skillName,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                )
                                Badge(containerColor = if (card.status == SkillStatus.LEARNED) Color(0xFF2E7D32) else Color(0xFFE65100)) {
                                    Text(card.status.name, color = Color.White)
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Overall Score: ${(card.scoreBreakdown.overallScore * 100).toInt()}%")
                        }
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = onStartNew,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Start Learning")
        }
    }
}

@Composable
fun StartLearningScreen(
    onStartSession: (LearningGoal) -> Unit,
    onCancel: () -> Unit
) {
    var goalText by remember { mutableStateOf("Learn Java HashMap well enough to use it correctly.") }
    var selectedPreset by remember { mutableStateOf("Java HashMap") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Start Autonomous Learning", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)

        Text("Select Preset Goal or Enter Custom:")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = selectedPreset == "Java HashMap",
                onClick = {
                    selectedPreset = "Java HashMap"
                    goalText = "Learn Java HashMap well enough to use it correctly."
                },
                label = { Text("Java HashMap") }
            )
            FilterChip(
                selected = selectedPreset == "Spring Boot",
                onClick = {
                    selectedPreset = "Spring Boot"
                    goalText = "Learn Spring Boot REST Controllers"
                },
                label = { Text("Spring Boot") }
            )
            FilterChip(
                selected = selectedPreset == "Git",
                onClick = {
                    selectedPreset = "Git"
                    goalText = "Learn Git Branching and Merging"
                },
                label = { Text("Git") }
            )
        }

        OutlinedTextField(
            value = goalText,
            onValueChange = { goalText = it },
            label = { Text("Learning Goal") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Reference Documents (Built-in)", fontWeight = FontWeight.Bold)
                Text("Primary: demo/references/hashmap_notes.md, java_collections.txt, hashmap_examples.md")
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(onClick = onCancel) {
                Text("Cancel")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = {
                    onStartSession(LearningGoal(id = UUID.randomUUID().toString(), goal = goalText))
                }
            ) {
                Text("START LEARNING")
            }
        }
    }
}

@Composable
fun LiveSessionScreen(
    goal: LearningGoal,
    skillRepository: InMemorySkillRepository,
    onCompleted: (SkillCard) -> Unit
) {
    var currentState by remember { mutableStateOf(AgentState.INITIALIZING) }
    val logMessages = remember { mutableStateListOf<String>() }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        coroutineScope.launch(Dispatchers.IO) {
            val knowledgeRepository = InMemoryKnowledgeRepository()
            val documentManager = DocumentManager()

            // Safe fallback reference content for mobile/JVM host execution
            val defaultReferenceContent = mapOf(
                "hashmap_notes.md" to ("# Java HashMap Guide\n- Overview of HashMap: key value mapping\n- Hashing: derives bucket index\n- Collision Handling: uses linked lists\n- equals and hashCode: contract requirement\n- Null keys: permits one null key\n- Iteration: entrySet iteration"),
                "springboot_rest.md" to ("# Spring Boot REST Guide\n- Controller Fundamentals: @RestController annotation\n- Request Mapping Annotations: @GetMapping and @PostMapping\n- Response Handling: ResponseEntity wrapper"),
                "git_branching.md" to ("# Git Branching Guide\n- Branch Management: git branch and checkout\n- Merging and Conflict Resolution: git merge and conflict resolution")
            )

            val refFiles = listOf(
                File("demo/references/hashmap_notes.md") to ("hashmap_notes.md" to "MD"),
                File("demo/references/java_collections.txt") to ("java_collections.txt" to "TXT"),
                File("demo/references/hashmap_examples.md") to ("hashmap_examples.md" to "MD"),
                File("demo/references/springboot_rest.md") to ("springboot_rest.md" to "MD"),
                File("demo/references/git_branching.md") to ("git_branching.md" to "MD")
            )

            var loadedCount = 0
            refFiles.forEach { (file, info) ->
                val (fileName, format) = info
                if (file.exists()) {
                    val (doc, passages) = documentManager.processDocument(
                        id = fileName,
                        title = fileName,
                        fileName = fileName,
                        format = format,
                        inputStreamProvider = { file.inputStream() }
                    )
                    knowledgeRepository.saveDocument(doc)
                    knowledgeRepository.savePassages(passages)
                    loadedCount++
                }
            }

            if (loadedCount == 0) {
                defaultReferenceContent.forEach { (fileName, text) ->
                    val (doc, passages) = documentManager.processDocument(
                        id = fileName,
                        title = fileName,
                        fileName = fileName,
                        format = "MD",
                        inputStreamProvider = { ByteArrayInputStream(text.toByteArray()) }
                    )
                    knowledgeRepository.saveDocument(doc)
                    knowledgeRepository.savePassages(passages)
                }
            }

            val controller = AgentController(
                knowledgeRepository = knowledgeRepository,
                skillRepository = skillRepository,
                reasoningProvider = MockReasoningProvider("MockChatGPT"),
                webResearchProvider = MockWebResearchProvider(),
                eventListener = object : AgentEventListener {
                    override fun onEvent(event: AgentEvent) {
                        when (event) {
                            is AgentEvent.StateChanged -> currentState = event.newState
                            is AgentEvent.LogMessage -> logMessages.add(event.message)
                            is AgentEvent.KnowledgeGapDetected -> logMessages.add("Gap Detected: ${event.gap.conceptOrTopic}")
                            else -> {}
                        }
                    }
                }
            )

            val resultCard = controller.executeLearningSession(goal)
            withContext(Dispatchers.Main) {
                onCompleted(resultCard)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text("Autonomous Learning in Progress", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Goal: ${goal.goal}", style = MaterialTheme.typography.bodyLarge)

        Spacer(modifier = Modifier.height(16.dp))
        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())

        Spacer(modifier = Modifier.height(16.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Current Phase:", fontWeight = FontWeight.Bold)
                Text(currentState.name, fontSize = 20.sp, color = MaterialTheme.colorScheme.primary)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text("Execution Event Log:", fontWeight = FontWeight.Bold)
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(8.dp)
        ) {
            items(logMessages) { msg ->
                Text("• $msg", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
fun SkillDetailScreen(
    card: SkillCard,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(card.skillName, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Badge(containerColor = if (card.status == SkillStatus.LEARNED) Color(0xFF2E7D32) else Color(0xFFE65100)) {
                Text(card.status.name, color = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Overall Score: ${(card.scoreBreakdown.overallScore * 100).toInt()}%", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                Text("Knowledge Coverage: ${(card.scoreBreakdown.knowledgeCoverage * 100).toInt()}%")
                Text("Practical Performance: ${(card.scoreBreakdown.practicalPerformance * 100).toInt()}%")
                Text("Conceptual Understanding: ${(card.scoreBreakdown.conceptualUnderstanding * 100).toInt()}%")
                Text("Source Confidence: ${(card.scoreBreakdown.sourceConfidence * 100).toInt()}%")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text("Source Provenance:", fontWeight = FontWeight.Bold)
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            items(card.referenceSources) { ref ->
                Text("• [${ref.sourceType}] ${ref.title} (${ref.location ?: "N/A"})")
            }
        }

        Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
            Text("Back to Skills List")
        }
    }
}
