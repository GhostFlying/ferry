package io.github.ghostflying.ferry.source

import java.io.InputStream
import java.time.Instant

/** A read-only source file, addressed by its slash-separated relative path. */
data class SourceEntry(
    val relativePath: String,
    val name: String,
    val size: Long,
    val modifiedAt: Instant,
)

/** Read-only access to an authorized source directory. */
interface SourceTree {
    /** Whether the directory can be enumerated with the persisted grant. */
    fun isAccessible(): Boolean

    fun list(): List<SourceEntry>

    fun open(relativePath: String): InputStream
}

/** Selects the source files to import; implemented by the Go rule planner. */
fun interface Planner {
    fun plan(configJson: String, files: List<SourceEntry>): Set<String>
}
