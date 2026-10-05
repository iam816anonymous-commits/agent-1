package com.skillbuilder.infrastructure.providers

import com.skillbuilder.core.domain.contracts.ReasoningRequest
import com.skillbuilder.core.domain.model.DocumentPassage
import com.skillbuilder.core.domain.model.SourceType
import org.junit.Assert.*
import org.junit.Test

class ProvidersTest {

    @Test
    fun testMockReasoningProviderWhenAvailable() {
        val provider = MockReasoningProvider("MockChatGPT", available = true)
        assertTrue(provider.isAvailable())

        val response = provider.requestReasoning(
            object : ReasoningRequest {
                override val purpose = "Test"
                override val query = "Explain Hash"
                override val contextPassages: List<DocumentPassage> = emptyList()
            }
        )

        assertEquals("MockChatGPT", response.providerName)
        assertEquals(SourceType.CHATGPT, response.provenance.sourceType)
    }

    @Test
    fun testApiReasoningProviderUnavailableWithoutKey() {
        val provider = ApiReasoningProvider("Gemini") { null }
        assertFalse(provider.isAvailable())
    }

    @Test
    fun testMockWebResearchProvider() {
        val webProvider = MockWebResearchProvider(available = true)
        assertTrue(webProvider.isAvailable())

        val results = webProvider.performResearch(
            com.skillbuilder.core.domain.contracts.WebResearchRequest("HashMap", "Verify null keys")
        )

        assertEquals(1, results.size)
        assertEquals(SourceType.OFFICIAL_WEB, results[0].provenance.sourceType)
    }
}
