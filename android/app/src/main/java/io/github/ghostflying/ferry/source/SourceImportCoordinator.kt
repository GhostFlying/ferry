package io.github.ghostflying.ferry.source

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.security.MessageDigest
import kotlinx.coroutines.ensureActive
import kotlin.coroutines.coroutineContext
import java.util.regex.Pattern

data class ImportedFile(
    val sourcePath: String,
    val privateCopy: File,
    val size: Long,
    val sha256: String,
)

class SourceImportCoordinator(
    private val context: Context,
    private val spoolRoot: File,
) {
    suspend fun importTree(treeUri: Uri, operationId: String): List<ImportedFile> {
        require(operationIdPattern.matcher(operationId).matches()) { "invalid operation id" }
        val root = DocumentFile.fromTreeUri(context, treeUri)
            ?: error("source authorization is unavailable")
        val operationRoot = File(spoolRoot, operationId).canonicalFile.apply { mkdirs() }
        require(operationRoot.toPath().startsWith(spoolRoot.canonicalFile.toPath())) { "operation path escaped spool" }
        return walk(root, operationRoot, "")
    }

    private suspend fun walk(
        directory: DocumentFile,
        operationRoot: File,
        prefix: String,
    ): List<ImportedFile> {
        val results = mutableListOf<ImportedFile>()
        for (child in directory.listFiles()) {
            coroutineContext.ensureActive()
            val name = child.name ?: continue
            require(safeSegment(name)) { "unsafe source name: $name" }
            val relative = if (prefix.isEmpty()) name else "$prefix/$name"
            if (child.isDirectory) {
                results += walk(child, operationRoot, relative)
            } else if (child.isFile) {
                results += copyComplete(child, relative, operationRoot)
            }
        }
        return results
    }

    private suspend fun copyComplete(source: DocumentFile, relative: String, operationRoot: File): ImportedFile {
        val destination = File(operationRoot, relative).canonicalFile
        require(destination.toPath().startsWith(operationRoot.toPath())) { "source path escaped spool" }
        require(!destination.exists()) { "private copy already exists: $relative" }
        destination.parentFile?.mkdirs()
        val advertisedSize = source.length()
        if (advertisedSize >= 0) {
            require(operationRoot.usableSpace >= advertisedSize) { "insufficient private storage" }
        }
        val partial = File(destination.path + ".partial")
        val digest = MessageDigest.getInstance("SHA-256")
        var size = 0L
        context.contentResolver.openInputStream(source.uri)?.use { input ->
            FileOutputStream(partial).use { output ->
                val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                while (true) {
                    coroutineContext.ensureActive()
                    val count = input.read(buffer)
                    if (count < 0) break
                    require((partial.parentFile?.usableSpace ?: 0L) >= count) { "insufficient private storage" }
                    output.write(buffer, 0, count)
                    digest.update(buffer, 0, count)
                    size += count
                }
                output.fd.sync()
            }
        } ?: error("source cannot be opened: $relative")
        if (!partial.renameTo(destination)) error("cannot commit private copy: $relative")
        return ImportedFile(relative, destination, size, digest.digest().hex())
    }

    fun reconcilePartials(operationId: String) {
        require(operationIdPattern.matcher(operationId).matches()) { "invalid operation id" }
        val operationRoot = File(spoolRoot, operationId).canonicalFile
        require(operationRoot.toPath().startsWith(spoolRoot.canonicalFile.toPath())) { "operation path escaped spool" }
        operationRoot.walkTopDown().filter { it.isFile && it.name.endsWith(".partial") }.forEach { it.delete() }
    }

    suspend fun hashPrivateCopy(file: File): String = MessageDigest.getInstance("SHA-256").let { digest ->
        FileInputStream(file).use { input ->
            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
            while (true) {
                coroutineContext.ensureActive()
                val count = input.read(buffer)
                if (count < 0) break
                digest.update(buffer, 0, count)
            }
        }
        digest.digest().hex()
    }

    private fun ByteArray.hex(): String = joinToString("") { "%02x".format(it.toInt() and 0xff) }

    private fun safeSegment(value: String): Boolean =
        value.isNotEmpty() && value != "." && value != ".." && !value.contains('/') && !value.contains('\\')

    private companion object {
        val operationIdPattern: Pattern = Pattern.compile("[A-Za-z0-9._-]{1,80}")
    }
}
