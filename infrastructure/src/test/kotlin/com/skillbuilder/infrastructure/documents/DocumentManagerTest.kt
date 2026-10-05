package com.skillbuilder.infrastructure.documents

import org.junit.Assert.*
import org.junit.Test

class DocumentManagerTest {

    @Test
    fun testTxtDocumentIngestion() {
        val reader = TxtDocumentReader()
        val text = "Paragraph 1 line A.\n\nParagraph 2 line B."
        val passages = reader.extractPassages("sample.txt") { text.byteInputStream() }

        assertEquals(2, passages.size)
        assertEquals("Paragraph 1", passages[0].sectionOrPage)
        assertEquals("Paragraph 1 line A.", passages[0].text)
        assertEquals("Paragraph 2", passages[1].sectionOrPage)
    }

    @Test
    fun testMarkdownDocumentIngestion() {
        val reader = MarkdownDocumentReader()
        val mdText = "# Section 1\nContent A\n\n## Section 2\nContent B"
        val passages = reader.extractPassages("sample.md") { mdText.byteInputStream() }

        assertEquals(2, passages.size)
        assertEquals("Section 1", passages[0].sectionOrPage)
        assertTrue(passages[0].text.contains("Content A"))
        assertEquals("Section 2", passages[1].sectionOrPage)
        assertTrue(passages[1].text.contains("Content B"))
    }

    @Test
    fun testDocumentManagerErrorHandling() {
        val manager = DocumentManager()
        val (doc, passages) = manager.processDocument(
            id = "1",
            title = "Unsupported",
            fileName = "file.unknown",
            format = "UNKNOWN",
            inputStreamProvider = { "".byteInputStream() }
        )

        assertEquals("Unsupported format: UNKNOWN", doc.errorMessage)
        assertTrue(passages.isEmpty())
    }
}
