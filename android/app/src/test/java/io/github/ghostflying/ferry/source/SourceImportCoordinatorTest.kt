package io.github.ghostflying.ferry.source

import io.github.ghostflying.ferry.config.StoredConfig
import io.github.ghostflying.ferry.data.OperationDao
import io.github.ghostflying.ferry.data.OperationEntity
import io.github.ghostflying.ferry.data.OperationRepository
import java.io.ByteArrayInputStream
import java.io.File
import java.io.InputStream
import java.security.MessageDigest
import java.time.Instant
import java.util.Collections
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class SourceImportCoordinatorTest {
    @get:Rule
    val folder = TemporaryFolder()

    private val dao = FakeOperationDao()
    private val repository = OperationRepository(dao)
    private val spool by lazy { folder.newFolder("spool") }
    private val partial by lazy { folder.newFolder("partial") }
    private val importer by lazy { SourceImportCoordinator(spool, partial, repository) }

    @Test
    fun importsOnlyPlannedFilesAndRegistersEligibleOperations() = runBlocking {
        val tree = FakeTree("DCIM/a.mp4" to "alpha", "DCIM/b.jpg" to "beta")

        val summary = importer.importPlanned(tree, planOnly("DCIM/a.mp4"), CONFIG, revision = 3)

        assertEquals(ImportSummary(imported = 1), summary)
        assertFalse(File(spool, "DCIM/b.jpg").exists())
        val operation = repository.eligibleOperations().single()
        assertEquals("DCIM/a.mp4", operation.sourcePath)
        assertEquals(3, operation.revision)
        assertEquals(sha256("alpha"), operation.sourceSha256)
        assertEquals("alpha", File(operation.privateCopy).readText())
    }

    @Test
    fun emptyPlanImportsNothing() = runBlocking {
        val summary = importer.importPlanned(FakeTree("DCIM/a.mp4" to "alpha"), planOnly(), CONFIG, revision = 1)

        assertEquals(ImportSummary(), summary)
        assertTrue(dao.operations.isEmpty())
    }

    @Test
    fun sizeMismatchFailsWithoutCompleteCopy() = runBlocking {
        val tree = FakeTree("DCIM/short.mp4" to "abc", "DCIM/unknown.mp4" to "abc", "DCIM/ok.mp4" to "ok")
        tree.declaredSizes["DCIM/short.mp4"] = 10
        tree.declaredSizes["DCIM/unknown.mp4"] = 0

        val summary = importer.importPlanned(tree, planAll(tree), CONFIG, revision = 1)

        assertEquals(ImportSummary(imported = 1, failed = 2), summary)
        assertFalse(File(spool, "DCIM/short.mp4").exists())
        assertFalse(File(spool, "DCIM/unknown.mp4").exists())
        assertEquals(listOf("DCIM/ok.mp4"), dao.operations.map { it.sourcePath })
        assertTrue(partial.listFiles()!!.isEmpty())
    }

    @Test
    fun cancellationLeavesNoCompleteCopyAndReconcileClearsPartials() = runBlocking {
        File(partial, "stale").writeText("left by a killed process")
        val reading = CompletableDeferred<Unit>()
        val release = CompletableDeferred<Unit>()
        val tree = FakeTree("DCIM/a.mp4" to "alpha")
        tree.streams["DCIM/a.mp4"] = { BlockingStream(reading, release) }

        val job: Job = CoroutineScope(SupervisorJob() + Dispatchers.Default).launch {
            importer.importPlanned(tree, planAll(tree), CONFIG, revision = 1)
        }
        reading.await()
        job.cancel()
        release.complete(Unit)
        job.join()
        importer.reconcilePartials()

        assertFalse(File(spool, "DCIM/a.mp4").exists())
        assertTrue(dao.operations.isEmpty())
        assertTrue(partial.listFiles()!!.isEmpty())
    }

    @Test
    fun secondPassSkipsKnownFilesAndRegistersUnrecordedCopy() = runBlocking {
        val tree = FakeTree("DCIM/a.mp4" to "alpha", "DCIM/b.mp4" to "beta", "DCIM/c.mp4" to "gamma")
        importer.importPlanned(FakeTree("DCIM/a.mp4" to "alpha", "DCIM/b.mp4" to "beta"), planAll(tree), CONFIG, revision = 1)
        // A copy committed before the process died, without an operation row.
        File(spool, "DCIM/c.mp4").apply { parentFile!!.mkdirs() }.writeText("gamma")
        tree.streams["DCIM/c.mp4"] = { error("a committed copy must not be read from the source again") }

        val summary = importer.importPlanned(tree, planAll(tree), CONFIG, revision = 1)

        assertEquals(ImportSummary(skipped = 2, registered = 1), summary)
        assertEquals(sha256("gamma"), dao.operations.single { it.sourcePath == "DCIM/c.mp4" }.sourceSha256)
    }

    @Test
    fun pocketHintRequiresSeenPocketAndInaccessibleDirectory() {
        assertNull(sourceAvailability(pocketSeen = true, treeAccessible = true))
        assertNull(sourceAvailability(pocketSeen = false, treeAccessible = true))
        assertEquals(SourceStatus.PocketNeedsOtg, sourceAvailability(pocketSeen = true, treeAccessible = false))
        assertEquals(SourceStatus.NeedsReselect, sourceAvailability(pocketSeen = false, treeAccessible = false))
    }

    @Test
    fun stageReportsPocketHintWithoutScanning() = runBlocking {
        val tree = FakeTree("DCIM/a.mp4" to "alpha").apply { accessible = false }
        val stage = SourceStage({ StoredConfig(CONFIG, 1) }, { tree }, planAll(tree), importer, pocketSeen = { true }, mountSettleMillis = 1_000)

        stage.run()

        assertEquals(SourceStatus.PocketNeedsOtg, stage.status.value)
        assertTrue(dao.operations.isEmpty())
    }

    @Test
    fun stageWaitsForCardToMountAfterPocketAttaches() = runBlocking {
        val tree = FakeTree("DCIM/a.mp4" to "alpha").apply { accessible = false }
        val stage = SourceStage({ StoredConfig(CONFIG, 1) }, { tree }, planAll(tree), importer, pocketSeen = { true })
        CoroutineScope(Dispatchers.Default).launch {
            delay(1_200)
            tree.accessible = true
        }

        stage.run()

        assertEquals(SourceStatus.Ready(ImportSummary(imported = 1)), stage.status.value)
    }

    @Test
    fun stageWithoutConfigurationDoesNotScan() = runBlocking {
        val tree = FakeTree("DCIM/a.mp4" to "alpha")
        val stage = SourceStage({ null }, { tree }, planAll(tree), importer, pocketSeen = { false })

        stage.run()

        assertEquals(SourceStatus.NotConfigured, stage.status.value)
        assertTrue(dao.operations.isEmpty())
    }

    @Test
    fun stageImportsWhenDirectoryIsAccessible() = runBlocking {
        val tree = FakeTree("DCIM/a.mp4" to "alpha")
        val stage = SourceStage({ StoredConfig(CONFIG, 1) }, { tree }, planAll(tree), importer, pocketSeen = { true })

        stage.run()

        assertEquals(SourceStatus.Ready(ImportSummary(imported = 1)), stage.status.value)
    }

    private fun planOnly(vararg paths: String) = Planner { _, _ -> paths.toSet() }

    private fun planAll(tree: FakeTree) = Planner { _, files -> files.mapTo(mutableSetOf()) { it.relativePath } }

    private fun sha256(value: String): String =
        MessageDigest.getInstance("SHA-256").digest(value.toByteArray()).joinToString("") { "%02x".format(it) }

    private class FakeTree(vararg files: Pair<String, String>) : SourceTree {
        val contents = linkedMapOf(*files)
        val declaredSizes = mutableMapOf<String, Long>()
        val streams = mutableMapOf<String, () -> InputStream>()
        @Volatile
        var accessible = true

        override fun isAccessible(): Boolean = accessible

        override fun list(): List<SourceEntry> = contents.map { (path, data) ->
            SourceEntry(path, path.substringAfterLast('/'), declaredSizes[path] ?: data.length.toLong(), Instant.EPOCH)
        }

        override fun open(relativePath: String): InputStream =
            streams[relativePath]?.invoke() ?: ByteArrayInputStream(contents.getValue(relativePath).toByteArray())
    }

    /** Returns one byte, then blocks until released so the reader can be cancelled mid-copy. */
    private class BlockingStream(
        private val reading: CompletableDeferred<Unit>,
        private val release: CompletableDeferred<Unit>,
    ) : InputStream() {
        private var first = true

        override fun read(): Int = error("unused")

        override fun read(buffer: ByteArray, offset: Int, length: Int): Int {
            if (first) {
                first = false
                buffer[offset] = 'a'.code.toByte()
                return 1
            }
            reading.complete(Unit)
            runBlocking { release.await() }
            buffer[offset] = 'l'.code.toByte()
            return 1
        }
    }

    private class FakeOperationDao : OperationDao {
        val operations: MutableList<OperationEntity> = Collections.synchronizedList(mutableListOf())

        override suspend fun find(id: String) = operations.find { it.id == id }
        override suspend fun findBySourcePath(sourcePath: String) = operations.find { it.sourcePath == sourcePath }
        override suspend fun findAll() = operations.toList()
        override suspend fun findEligible() = operations.filter {
            it.phase in setOf("imported", "waiting") && !it.manualPaused && it.sourceSha256.isNotEmpty() && it.privateCopy.isNotEmpty()
        }
        override suspend fun save(operation: OperationEntity) {
            operations.removeAll { it.id == operation.id }
            operations += operation
        }
        override suspend fun claimForUpload(id: String, revision: Long, expectedPhase: String, updatedAt: Long) = 0
        override suspend fun setManualPause(id: String, revision: Long, updatedAt: Long) = 0
        override suspend fun markSystemWaiting(updatedAt: Long) = 0
        override suspend fun recoverInterrupted(updatedAt: Long) = 0
        override suspend fun markCompleted(id: String, revision: Long, remoteSha256: String, updatedAt: Long) = 0
        override suspend fun markFailed(id: String, revision: Long, error: String, updatedAt: Long) = 0
    }

    private companion object {
        const val CONFIG = "{}"
    }
}
