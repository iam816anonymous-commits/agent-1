package com.skillbuilder.infrastructure.documents

import com.skillbuilder.core.domain.contracts.DocumentReader
import com.skillbuilder.core.domain.model.Document
import com.skillbuilder.core.domain.model.DocumentPassage
import com.skillbuilder.core.domain.model.ExtractionStatus
import java.io.InputStream

class DocumentManager(
    private val readers: List<DocumentReader> = listOf(
        TxtDocumentReader(),
        MarkdownDocumentReader(),
        PdfDocumentReader()
    )
) {
    fun processDocument(
        id: String,
        title: String,
        fileName: String,
        format: String,
        inputStreamProvider: () -> InputStream
    ): Pair<Document, List<DocumentPassage>> {
        val reader = readers.firstOrNull { it.supportsFormat(format) }
            ?: return Pair(
                Document(
                    id = id,
                    title = title,
                    fileName = fileName,
                    format = format,
                    status = ExtractionStatus.ERROR,
                    errorMessage = "Unsupported format: $format"
                ),
                emptyList()
            )

        return try {
            val passages = reader.extractPassages(fileName, inputStreamProvider)
            if (passages.isEmpty()) {
                Pair(
                    Document(
                        id = id,
                        title = title,
                        fileName = fileName,
                        format = format,
                        status = ExtractionStatus.TEXT_EXTRACTION_UNAVAILABLE,
                        errorMessage = "No readable text extracted"
                    ),
                    emptyList()
                )
            } else {
                Pair(
                    Document(
                        id = id,
                        title = title,
                        fileName = fileName,
                        format = format,
                        status = ExtractionStatus.SUCCESS
                    ),
                    passages
                )
            }
        } catch (e: Exception) {
            Pair(
                Document(
                    id = id,
                    title = title,
                    fileName = fileName,
                    format = format,
                    status = ExtractionStatus.ERROR,
                    errorMessage = e.message ?: "Extraction failed"
                ),
                emptyList()
            )
        }
    }
}
