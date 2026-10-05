package com.skillbuilder.infrastructure.providers

import com.skillbuilder.core.domain.contracts.WebResearchProvider
import com.skillbuilder.core.domain.contracts.WebResearchRequest
import com.skillbuilder.core.domain.contracts.WebResearchResponse
import com.skillbuilder.core.domain.model.SourceProvenance
import com.skillbuilder.core.domain.model.SourceType

class MockWebResearchProvider(
    private val available: Boolean = true
) : WebResearchProvider {
    override fun isAvailable(): Boolean = available

    override fun performResearch(request: WebResearchRequest): List<WebResearchResponse> {
        if (!available) return emptyList()

        return listOf(
            WebResearchResponse(
                url = "https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/HashMap.html",
                title = "HashMap (Java SE 21 & JDK 21)",
                snippet = "Hash table based implementation of the Map interface. This implementation provides all of the optional map operations, and permits null values and the null key.",
                sourceDomain = "docs.oracle.com",
                provenance = SourceProvenance(
                    sourceType = SourceType.OFFICIAL_WEB,
                    title = "Official Oracle Java Documentation",
                    url = "https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/HashMap.html"
                )
            )
        )
    }
}
