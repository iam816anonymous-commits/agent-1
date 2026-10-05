package com.skillbuilder.core.domain.model

enum class ExtractionStatus {
    SUCCESS,
    TEXT_EXTRACTION_UNAVAILABLE,
    ERROR
}

data class Document(
    val id: String,
    val title: String,
    val fileName: String,
    val format: String,
    val status: ExtractionStatus = ExtractionStatus.SUCCESS,
    val errorMessage: String? = null,
    val metadata: Map<String, String> = emptyMap(),
    val timestamp: Long = System.currentTimeMillis()
)

data class DocumentPassage(
    val id: String,
    val documentId: String,
    val sectionOrPage: String,
    val text: String,
    val provenance: SourceProvenance
)
