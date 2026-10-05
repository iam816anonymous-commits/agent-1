package com.skillbuilder.infrastructure.providers

import com.skillbuilder.core.domain.contracts.ReasoningProvider
import com.skillbuilder.core.domain.contracts.ReasoningRequest
import com.skillbuilder.core.domain.contracts.ReasoningResponse
import com.skillbuilder.core.domain.model.SourceProvenance
import com.skillbuilder.core.domain.model.SourceType

class MockReasoningProvider(
    override val providerName: String = "MockChatGPT",
    private val available: Boolean = true
) : ReasoningProvider {
    override fun isAvailable(): Boolean = available

    override fun requestReasoning(request: ReasoningRequest): ReasoningResponse {
        check(available) { "Provider $providerName is currently unavailable" }
        return ReasoningResponse(
            providerName = providerName,
            explanation = "Mock explanation for ${request.query}. Context provided: ${request.contextPassages.size} passages.",
            identifiedConcepts = listOf("Mock Concept A", "Mock Concept B"),
            confidence = 0.90f,
            provenance = SourceProvenance(
                sourceType = if (providerName.contains("Gemini", ignoreCase = true)) SourceType.GEMINI else SourceType.CHATGPT,
                title = providerName,
                location = "Mock Reasoning Output"
            )
        )
    }
}
