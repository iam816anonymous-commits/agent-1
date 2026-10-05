package com.skillbuilder.infrastructure.providers

import com.skillbuilder.core.domain.contracts.ReasoningProvider
import com.skillbuilder.core.domain.contracts.ReasoningRequest
import com.skillbuilder.core.domain.contracts.ReasoningResponse
import com.skillbuilder.core.domain.model.SourceProvenance
import com.skillbuilder.core.domain.model.SourceType

class ApiReasoningProvider(
    override val providerName: String,
    private val apiKeySupplier: () -> String?
) : ReasoningProvider {

    override fun isAvailable(): Boolean {
        val key = apiKeySupplier()
        return !key.isNullOrBlank()
    }

    override fun requestReasoning(request: ReasoningRequest): ReasoningResponse {
        val key = apiKeySupplier()
        if (key.isNullOrBlank()) {
            throw IllegalStateException("$providerName API key is missing or not configured.")
        }

        val sourceType = if (providerName.contains("Gemini", ignoreCase = true)) SourceType.GEMINI else SourceType.CHATGPT

        return ReasoningResponse(
            providerName = providerName,
            explanation = "API response for ${request.query} using supplied context (${request.contextPassages.size} passages).",
            identifiedConcepts = listOf("API Concept"),
            confidence = 0.95f,
            provenance = SourceProvenance(
                sourceType = sourceType,
                title = providerName,
                location = "API Response"
            )
        )
    }
}
