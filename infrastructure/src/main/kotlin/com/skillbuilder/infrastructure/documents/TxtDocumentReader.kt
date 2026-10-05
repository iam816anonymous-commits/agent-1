package com.skillbuilder.infrastructure.documents

import com.skillbuilder.core.domain.contracts.DocumentReader
import com.skillbuilder.core.domain.model.DocumentPassage
import com.skillbuilder.core.domain.model.SourceProvenance
import com.skillbuilder.core.domain.model.SourceType
import java.io.InputStream
import java.util.UUID

class TxtDocumentReader : DocumentReader {
    override fun supportsFormat(format: String): Boolean {
        return format.equals("TXT", ignoreCase = true) || format.equals("TEXT", ignoreCase = true)
    }

    override fun extractPassages(
        fileName: String,
        inputStreamProvider: () -> InputStream
    ): List<DocumentPassage> {
        val content = inputStreamProvider().bufferedReader().use { it.readText() }
        if (content.isBlank()) return emptyList()

        val paragraphs = content.split(Regex("\n\\s*\n"))
        val passages = mutableListOf<DocumentPassage>()

        paragraphs.forEachIndexed { index, para ->
            val cleanPara = para.trim()
            if (cleanPara.isNotBlank()) {
                passages.add(
                    DocumentPassage(
                        id = UUID.randomUUID().toString(),
                        documentId = fileName,
                        sectionOrPage = "Paragraph ${index + 1}",
                        text = cleanPara,
                        provenance = SourceProvenance(
                            sourceType = SourceType.REFERENCE,
                            documentId = fileName,
                            title = fileName,
                            location = "Paragraph ${index + 1}"
                        )
                    )
                )
            }
        }
        return passages
    }
}
