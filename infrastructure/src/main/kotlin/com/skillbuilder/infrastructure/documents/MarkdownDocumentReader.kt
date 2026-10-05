package com.skillbuilder.infrastructure.documents

import com.skillbuilder.core.domain.contracts.DocumentReader
import com.skillbuilder.core.domain.model.DocumentPassage
import com.skillbuilder.core.domain.model.SourceProvenance
import com.skillbuilder.core.domain.model.SourceType
import java.io.InputStream
import java.util.UUID

class MarkdownDocumentReader : DocumentReader {
    override fun supportsFormat(format: String): Boolean {
        return format.equals("MD", ignoreCase = true) || format.equals("MARKDOWN", ignoreCase = true)
    }

    override fun extractPassages(
        fileName: String,
        inputStreamProvider: () -> InputStream
    ): List<DocumentPassage> {
        val lines = inputStreamProvider().bufferedReader().use { it.readLines() }
        val passages = mutableListOf<DocumentPassage>()

        var currentSection = "Header/Intro"
        var currentBuffer = StringBuilder()

        fun flushBuffer() {
            val text = currentBuffer.toString().trim()
            if (text.isNotBlank()) {
                passages.add(
                    DocumentPassage(
                        id = UUID.randomUUID().toString(),
                        documentId = fileName,
                        sectionOrPage = currentSection,
                        text = text,
                        provenance = SourceProvenance(
                            sourceType = SourceType.REFERENCE,
                            documentId = fileName,
                            title = fileName,
                            location = currentSection
                        )
                    )
                )
            }
            currentBuffer = StringBuilder()
        }

        for (line in lines) {
            if (line.startsWith("#")) {
                flushBuffer()
                currentSection = line.trim('#', ' ').ifBlank { "Header" }
            } else {
                currentBuffer.append(line).append("\n")
            }
        }
        flushBuffer()

        return passages
    }
}
