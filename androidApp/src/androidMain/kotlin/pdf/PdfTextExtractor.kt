package com.banking.app.pdf

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper

/**
 * Turns a picked PDF into the flat list of text lines that
 * [utils.BankStatementParser] expects — the Android counterpart of the web
 * app's `pdfTextExtractor.js` (PDF.js).
 *
 * Call off the main thread; parsing a statement takes a moment.
 */
object PdfTextExtractor {

    fun extractLines(context: Context, uri: Uri): List<String> {
        // Cheap no-op after the first call; the parser needs the bundled fonts.
        PDFBoxResourceLoader.init(context.applicationContext)

        val input = context.contentResolver.openInputStream(uri)
            ?: error("PDF konnte nicht geöffnet werden")

        return input.use { stream ->
            PDDocument.load(stream).use { document ->
                // Statement rows only reconstruct correctly in visual order.
                val stripper = PDFTextStripper().apply { sortByPosition = true }
                stripper.getText(document)
                    .split("\n")
                    .map { it.trim() }
                    .filter { it.isNotEmpty() }
            }
        }
    }

    /** Human-readable file name for the picked document, for the success message. */
    fun displayName(context: Context, uri: Uri): String {
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (index >= 0 && cursor.moveToFirst()) {
                cursor.getString(index)?.let { return it }
            }
        }
        return uri.lastPathSegment ?: "Kontoauszug.pdf"
    }
}
