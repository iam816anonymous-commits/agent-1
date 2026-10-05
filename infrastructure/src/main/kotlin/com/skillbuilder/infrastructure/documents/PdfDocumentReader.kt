package com.skillbuilder.infrastructure.documents

import com.skillbuilder.core.domain.contracts.DocumentReader
import com.skillbuilder.core.domain.model.DocumentPassage
import com.skillbuilder.core.domain.model.SourceProvenance
import com.skillbuilder.core.domain.model.SourceType
import java.io.InputStream
import java.util.UUID

class PdfDocumentReader : DocumentReader {
    override fun supportsFormat(format: String): Boolean {
        return format.equals("PDF", ignoreCase = true)
    }

    override fun extractPassages(
        fileName: String,
        inputStreamProvider: () -> InputStream
    ): List<DocumentPassage> {
        val passages = mutableListOf<DocumentPassage>()
        val text = inputStreamProvider().bufferedReader().use { it.readText() }

        if (text.isNotBlank()) {
            val sections = text.split("\n\n")
            sections.forEachIndexed { index, section ->
                val clean = section.trim()
                if (clean.isNotBlank()) {
                    passages.add(
                        DocumentPassage(
                            id = UUID.randomUUID().toString(),
                            documentId = fileName,
                            sectionOrPage = "Page ${index + 1}",
                            text = clean,
                            provenance = SourceProvenance(
                                sourceType = SourceType.REFERENCE,
                                documentId = fileName,
                                title = fileName,
                                location = "Page ${index + 1}"
                            )
                        )
                    )
                }
            }
        }
        return passages
    }
}
