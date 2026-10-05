package com.skillbuilder.infrastructure.documents

import com.skillbuilder.core.domain.contracts.DocumentReader
import com.skillbuilder.core.domain.model.DocumentPassage
import com.skillbuilder.core.domain.model.SourceProvenance
import com.skillbuilder.core.domain.model.SourceType
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.text.PDFTextStripper
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
        inputStreamProvider().use { inputStream ->
            PDDocument.load(inputStream).use { document ->
                val pageCount = document.numberOfPages
                val stripper = PDFTextStripper()

                for (page in 1..pageCount) {
                    stripper.startPage = page
                    stripper.endPage = page
                    val pageText = stripper.getText(document).trim()

                    if (pageText.isNotBlank()) {
                        passages.add(
                            DocumentPassage(
                                id = UUID.randomUUID().toString(),
                                documentId = fileName,
                                sectionOrPage = "Page $page",
                                text = pageText,
                                provenance = SourceProvenance(
                                    sourceType = SourceType.REFERENCE,
                                    documentId = fileName,
                                    title = fileName,
                                    location = "Page $page"
                                )
                            )
                        )
                    }
                }
            }
        }
        return passages
    }
}
