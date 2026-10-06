package io.github.ghostflying.ferry.source

import io.github.ghostflying.ferry.data.OperationRepository
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.security.MessageDigest
import kotlinx.coroutines.ensureActive
import kotlin.coroutines.coroutineContext

/** Counts of one source pass; [failed] files have no operation and are retried on the next pass. */
data class ImportSummary(
    val imported: Int = 0,
    val skipped: Int = 0,
    val registered: Int = 0,
    val failed: Int = 0,
)

/**
 * Imports planned source files into complete private copies keyed by their
 * relative path, so a later pass skips files that already have an operation.
 * Partial copies live in [partialRoot], outside the spool, and are discarded
 * by [reconcilePartials].
 */
class SourceImportCoordinator(
    private val spoolRoot: File,
    private val partialRoot: File,
    private val repository: OperationRepository,
) {
    suspend fun importPlanned(
        tree: SourceTree,
        planner: Planner,
        configJson: String,
        revision: Long,
    ): ImportSummary {
        val entries = tree.list()
        val planned = planner.plan(configJson, entries)
        var summary = ImportSummary()
        for (entry in entries) {
            coroutineContext.ensureActive()
            if (entry.relativePath !in planned) continue
            if (repository.findBySourcePath(entry.relativePath) != null) {
                summary = summary.copy(skipped = summary.skipped + 1)
                continue
            }
            val destination = spoolFile(entry.relativePath)
            if (destination.exists()) {
                // Committed before the process died, but never registered.
                repository.createIntent(revision, entry.relativePath, destination.path, hashFile(destination))
                summary = summary.copy(registered = summary.registered + 1)
                continue
            }
            val sha256 = tree.open(entry.relativePath).use { copyComplete(it, entry, destination) }
            if (sha256 == null) {
                summary = summary.copy(failed = summary.failed + 1)
                continue
            }
            repository.createIntent(revision, entry.relativePath, destination.path, sha256)
            summary = summary.copy(imported = summary.imported + 1)
        }
        return summary
    }

    fun reconcilePartials() {
        partialRoot.listFiles()?.forEach { it.delete() }
    }

    /** Returns the SHA-256 of the committed copy, or null if the source size did not match. */
    private suspend fun copyComplete(input: InputStream, entry: SourceEntry, destination: File): String? {
        partialRoot.mkdirs()
        require(partialRoot.usableSpace >= entry.size) { "insufficient private storage" }
        val partial = File(partialRoot, sha256Hex(entry.relativePath.toByteArray()))
        var committed = false
        try {
            val digest = MessageDigest.getInstance("SHA-256")
            var size = 0L
            FileOutputStream(partial).use { output ->
                val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                while (true) {
                    coroutineContext.ensureActive()
                    val count = input.read(buffer)
                    if (count < 0) break
                    require(partialRoot.usableSpace >= count) { "insufficient private storage" }
                    output.write(buffer, 0, count)
                    digest.update(buffer, 0, count)
                    size += count
                }
                output.fd.sync()
            }
            if (size != entry.size) return null
            destination.parentFile?.mkdirs()
            if (!partial.renameTo(destination)) error("cannot commit private copy: ${entry.relativePath}")
            committed = true
            return digest.digest().hex()
        } finally {
            if (!committed) partial.delete()
        }
    }

    private fun spoolFile(relativePath: String): File {
        val file = File(spoolRoot, relativePath).canonicalFile
        require(file.toPath().startsWith(spoolRoot.canonicalFile.toPath())) { "source path escaped spool" }
        return file
    }

    private suspend fun hashFile(file: File): String {
        val digest = MessageDigest.getInstance("SHA-256")
        FileInputStream(file).use { input ->
            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
            while (true) {
                coroutineContext.ensureActive()
                val count = input.read(buffer)
                if (count < 0) break
                digest.update(buffer, 0, count)
            }
        }
        return digest.digest().hex()
    }

    private fun sha256Hex(value: ByteArray): String = MessageDigest.getInstance("SHA-256").digest(value).hex()

    private fun ByteArray.hex(): String = joinToString("") { "%02x".format(it.toInt() and 0xff) }
}
