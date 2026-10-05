package com.skillbuilder.core.domain.model

enum class SourceType {
    REFERENCE,
    OFFICIAL_WEB,
    CHATGPT,
    GEMINI,
    INFERENCE
}

data class SourceProvenance(
    val sourceType: SourceType,
    val documentId: String? = null,
    val title: String,
    val location: String? = null,
    val url: String? = null,
    val confidence: Float = 1.0f,
    val timestamp: Long = System.currentTimeMillis()
)
