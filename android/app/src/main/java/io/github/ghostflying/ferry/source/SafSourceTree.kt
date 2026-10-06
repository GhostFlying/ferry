package io.github.ghostflying.ferry.source

import android.content.ContentResolver
import android.net.Uri
import android.provider.DocumentsContract
import android.provider.DocumentsContract.Document
import java.io.FileNotFoundException
import java.io.InputStream
import java.time.Instant

/** Lists and opens files under a persisted SAF tree grant without writing to it. */
class SafSourceTree(
    private val resolver: ContentResolver,
    private val treeUri: Uri,
) : SourceTree {
    private val documents = mutableMapOf<String, Uri>()

    override fun isAccessible(): Boolean {
        val granted = resolver.persistedUriPermissions.any { it.uri == treeUri && it.isReadPermission }
        if (!granted) return false
        return try {
            resolver.query(childrenUri(DocumentsContract.getTreeDocumentId(treeUri)), arrayOf(Document.COLUMN_DOCUMENT_ID), null, null, null)
                ?.use { true } ?: false
        } catch (_: Exception) {
            // Providers throw for a volume that is no longer mounted.
            false
        }
    }

    override fun list(): List<SourceEntry> {
        documents.clear()
        val entries = mutableListOf<SourceEntry>()
        walk(DocumentsContract.getTreeDocumentId(treeUri), "", entries)
        return entries
    }

    override fun open(relativePath: String): InputStream {
        val uri = documents[relativePath] ?: throw FileNotFoundException(relativePath)
        return resolver.openInputStream(uri) ?: throw FileNotFoundException(relativePath)
    }

    private fun walk(documentId: String, prefix: String, entries: MutableList<SourceEntry>) {
        val cursor = resolver.query(childrenUri(documentId), PROJECTION, null, null, null)
            ?: throw FileNotFoundException("source directory is unavailable")
        cursor.use {
            while (it.moveToNext()) {
                val childId = it.getString(0)
                val name = it.getString(1) ?: continue
                require(isSafeSegment(name)) { "unsafe source name: $name" }
                val relative = if (prefix.isEmpty()) name else "$prefix/$name"
                if (it.getString(2) == Document.MIME_TYPE_DIR) {
                    walk(childId, relative, entries)
                } else {
                    documents[relative] = DocumentsContract.buildDocumentUriUsingTree(treeUri, childId)
                    val size = if (it.isNull(3)) 0L else it.getLong(3)
                    val modified = if (it.isNull(4)) 0L else it.getLong(4)
                    entries += SourceEntry(relative, name, size, Instant.ofEpochMilli(modified))
                }
            }
        }
    }

    private fun childrenUri(documentId: String): Uri =
        DocumentsContract.buildChildDocumentsUriUsingTree(treeUri, documentId)

    companion object {
        private val PROJECTION = arrayOf(
            Document.COLUMN_DOCUMENT_ID,
            Document.COLUMN_DISPLAY_NAME,
            Document.COLUMN_MIME_TYPE,
            Document.COLUMN_SIZE,
            Document.COLUMN_LAST_MODIFIED,
        )

        /** Human-readable directory name, e.g. `DCIM` for `XXXX-XXXX:DCIM`. */
        fun label(treeUri: Uri): String {
            val documentId = DocumentsContract.getTreeDocumentId(treeUri)
            return documentId.substringAfter(':').ifEmpty { documentId }
        }
    }
}

internal fun isSafeSegment(value: String): Boolean =
    value.isNotEmpty() && value != "." && value != ".." && !value.contains('/') && !value.contains('\\')
