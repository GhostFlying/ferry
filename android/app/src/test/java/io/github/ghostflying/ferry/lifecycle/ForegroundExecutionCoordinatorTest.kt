package io.github.ghostflying.ferry.lifecycle

import io.github.ghostflying.ferry.data.OperationDao
import io.github.ghostflying.ferry.data.OperationEntity
import io.github.ghostflying.ferry.data.OperationRepository
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import java.util.Collections
import java.util.concurrent.atomic.AtomicInteger
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ForegroundExecutionCoordinatorTest {
    private val clock = Clock.fixed(Instant.ofEpochMilli(1_000), ZoneOffset.UTC)

    @Test
    fun onOpenStartsAtMostOneActionAndOnStopPreventsAnother() = runBlocking {
        val firstStarted = CompletableDeferred<Unit>()
        val operations = listOf(operation("one"), operation("two"))
        val dao = FakeOperationDao(operations)
        val repository = OperationRepository(dao, clock)
        val calls = AtomicInteger()
        val coordinator = ForegroundExecutionCoordinator(
            repository,
            CoroutineScope(SupervisorJob() + Dispatchers.Default),
        ) { 
            calls.incrementAndGet()
            firstStarted.complete(Unit)
            delay(10_000)
            "a".repeat(64)
        }

        coordinator.onOpen()
        firstStarted.await()
        coordinator.onOpen()
        coordinator.onStop()

        assertEquals(1, calls.get())
        assertTrue(dao.operations.all { it.phase == "waiting" })
    }

    @Test
    fun onOpenRecoversUploadLeftByKilledProcess() = runBlocking {
        val dao = FakeOperationDao(listOf(operation("one", phase = "uploading")))
        val repository = OperationRepository(dao, clock)
        val coordinator = ForegroundExecutionCoordinator(
            repository,
            CoroutineScope(SupervisorJob() + Dispatchers.Default),
        ) { "a".repeat(64) }

        coordinator.onOpen()
        eventually { dao.operations.single().phase == "completed" }
        coordinator.onStop()
    }

    @Test
    fun sourceStageRunsWithoutUploader() = runBlocking {
        val ran = CompletableDeferred<Unit>()
        val coordinator = ForegroundExecutionCoordinator(
            OperationRepository(FakeOperationDao(emptyList()), clock),
            CoroutineScope(SupervisorJob() + Dispatchers.Default),
            sourceStage = { ran.complete(Unit) },
            upload = null,
        )

        coordinator.onOpen()
        ran.await()
        coordinator.onStop()
    }

    @Test
    fun manualPauseCancelsActiveActionAndSurvivesOpen() = runBlocking {
        val started = CompletableDeferred<Unit>()
        val finished = CompletableDeferred<Unit>()
        val dao = FakeOperationDao(listOf(operation("one", phase = "waiting")))
        val repository = OperationRepository(dao, clock)
        val coordinator = ForegroundExecutionCoordinator(
            repository,
            CoroutineScope(SupervisorJob() + Dispatchers.Default),
        ) {
            started.complete(Unit)
            try {
                delay(10_000)
                "a".repeat(64)
            } finally {
                finished.complete(Unit)
            }
        }

        coordinator.onOpen()
        started.await()
        assertTrue(coordinator.pause("one"))
        assertTrue(finished.isCompleted)
        coordinator.onOpen()
        delay(20)

        assertEquals("paused", dao.operations.single().phase)
        assertTrue(dao.operations.single().manualPaused)
        coordinator.onStop()
    }

    @Test
    fun staleManualPausePreventsClaimAndAction() = runBlocking {
        val dao = FakeOperationDao(listOf(operation("one", phase = "waiting")))
        val repository = OperationRepository(dao, clock)
        assertTrue(repository.pauseAndReport("one"))
        val calls = AtomicInteger()
        val coordinator = ForegroundExecutionCoordinator(
            repository,
            CoroutineScope(SupervisorJob() + Dispatchers.Default),
        ) {
            calls.incrementAndGet()
            "a".repeat(64)
        }

        coordinator.onOpen()
        delay(50)

        assertEquals(0, calls.get())
        assertEquals("paused", dao.operations.single().phase)
        coordinator.onStop()
    }

    @Test
    fun pauseBetweenEligibleReadAndClaimWinsCas() = runBlocking {
        val dao = FakeOperationDao(listOf(operation("one", phase = "waiting")))
        dao.pauseOnNextClaim = true
        val repository = OperationRepository(dao, clock)
        val calls = AtomicInteger()
        val coordinator = ForegroundExecutionCoordinator(
            repository,
            CoroutineScope(SupervisorJob() + Dispatchers.Default),
        ) {
            calls.incrementAndGet()
            "a".repeat(64)
        }

        coordinator.onOpen()
        delay(50)

        assertEquals(0, calls.get())
        assertEquals("paused", dao.operations.single().phase)
        coordinator.onStop()
    }

    @Test
    fun terminalOperationsAreIgnoredAndFinishedWorkerDoesNotRepeat() = runBlocking {
        val completed = operation("completed").copy(phase = "completed", remoteSha256 = "a".repeat(64))
        val failed = operation("failed").copy(phase = "failed", lastError = "previous")
        val dao = FakeOperationDao(listOf(completed, failed, operation("one")))
        val repository = OperationRepository(dao, clock)
        val calls = AtomicInteger()
        val coordinator = ForegroundExecutionCoordinator(
            repository,
            CoroutineScope(SupervisorJob() + Dispatchers.Default),
        ) {
            calls.incrementAndGet()
            "a".repeat(64)
        }

        coordinator.onOpen()
        eventually { dao.operations.find { it.id == "one" }?.phase == "completed" }
        coordinator.onOpen()
        delay(20)

        assertEquals(1, calls.get())
        assertEquals("completed", dao.operations.find { it.id == "one" }?.phase)
        coordinator.onStop()
    }

    @Test
    fun actionExceptionMarksUploadingOperationFailed() = runBlocking {
        val dao = FakeOperationDao(listOf(operation("one")))
        val repository = OperationRepository(dao, clock)
        val coordinator = ForegroundExecutionCoordinator(
            repository,
            CoroutineScope(SupervisorJob() + Dispatchers.Default),
        ) { error("injected upload failure") }

        coordinator.onOpen()
        eventually { dao.operations.single().phase == "failed" }

        assertEquals("failed", dao.operations.single().phase)
        assertTrue(dao.operations.single().lastError?.contains("injected") == true)
        coordinator.onStop()
    }

    @Test
    fun missingUploadActionDoesNotClaimOrFailReadyOperation() = runBlocking {
        val dao = FakeOperationDao(listOf(operation("one")))
        val repository = OperationRepository(dao, clock)
        val coordinator = ForegroundExecutionCoordinator(
            repository,
            CoroutineScope(SupervisorJob() + Dispatchers.Default),
            upload = null,
        )

        coordinator.onOpen()
        delay(50)

        assertEquals("imported", dao.operations.single().phase)
        assertEquals(null, dao.operations.single().lastError)
        coordinator.onStop()
    }

    @Test
    fun lateActionResultCannotCompletePausedOperation() = runBlocking {
        val started = CompletableDeferred<Unit>()
        val dao = FakeOperationDao(listOf(operation("one")))
        val repository = OperationRepository(dao, clock)
        val coordinator = ForegroundExecutionCoordinator(
            repository,
            CoroutineScope(SupervisorJob() + Dispatchers.Default),
        ) {
            started.complete(Unit)
            try {
                delay(10_000)
            } catch (_: CancellationException) {
                // Model a transport that returns a late result after cancellation.
            }
            "a".repeat(64)
        }

        coordinator.onOpen()
        started.await()
        assertTrue(coordinator.pause("one"))

        assertEquals("paused", dao.operations.single().phase)
        assertTrue(dao.operations.single().remoteSha256 == null)
        coordinator.onStop()
    }

    @Test
    fun remoteHashMismatchFailsWithoutDeletingPrivateCopy() = runBlocking {
        val dao = FakeOperationDao(listOf(operation("one")))
        val repository = OperationRepository(dao, clock)
        val coordinator = ForegroundExecutionCoordinator(
            repository,
            CoroutineScope(SupervisorJob() + Dispatchers.Default),
        ) { "b".repeat(64) }

        coordinator.onOpen()
        eventually { dao.operations.single().phase == "failed" }

        assertEquals("failed", dao.operations.single().phase)
        assertFalse(dao.operations.single().privateCopy.isEmpty())
        coordinator.onStop()
    }

    private fun operation(id: String, phase: String = "imported", manualPaused: Boolean = false) =
        OperationEntity(
            id = id,
            revision = 1,
            phase = phase,
            manualPaused = manualPaused,
            sourcePath = "DCIM/$id.MP4",
            privateCopy = "/private/$id",
            sourceSha256 = "a".repeat(64),
            remoteSha256 = null,
            lastError = null,
            updatedAt = 1_000,
        )

    private suspend fun eventually(condition: () -> Boolean) {
        repeat(100) {
            if (condition()) return
            delay(10)
        }
        error("condition did not become true")
    }

    private class FakeOperationDao(initial: List<OperationEntity>) : OperationDao {
        val operations = Collections.synchronizedList(initial.toMutableList())
        private val lock = Any()
        var pauseOnNextClaim = false

        override suspend fun find(id: String): OperationEntity? = synchronized(lock) {
            operations.find { it.id == id }
        }

        override suspend fun findBySourcePath(sourcePath: String): OperationEntity? = synchronized(lock) {
            operations.find { it.sourcePath == sourcePath }
        }

        override suspend fun findAll(): List<OperationEntity> = synchronized(lock) {
            operations.toList()
        }

        override suspend fun findEligible(): List<OperationEntity> = synchronized(lock) {
            operations.filter {
                it.phase in setOf("imported", "waiting") && !it.manualPaused &&
                    it.sourceSha256.isNotEmpty() && it.privateCopy.isNotEmpty()
            }
        }

        override suspend fun save(operation: OperationEntity) = synchronized(lock) {
            operations.removeAll { it.id == operation.id }
            operations += operation
        }

        override suspend fun claimForUpload(id: String, revision: Long, expectedPhase: String, updatedAt: Long): Int = synchronized(lock) {
            if (pauseOnNextClaim) {
                pauseOnNextClaim = false
                val pauseIndex = operations.indexOfFirst { it.id == id }
                if (pauseIndex >= 0) {
                    operations[pauseIndex] = operations[pauseIndex].copy(manualPaused = true, phase = "paused")
                }
            }
            val index = operations.indexOfFirst {
                it.id == id && it.revision == revision && it.phase == expectedPhase &&
                    !it.manualPaused && it.sourceSha256.isNotEmpty() && it.privateCopy.isNotEmpty()
            }
            if (index < 0) return 0
            operations[index] = operations[index].copy(phase = "uploading", updatedAt = updatedAt)
            return 1
        }

        override suspend fun setManualPause(id: String, revision: Long, updatedAt: Long): Int = synchronized(lock) {
            val index = operations.indexOfFirst {
                it.id == id && it.revision == revision && !it.manualPaused &&
                    it.phase in setOf("imported", "uploading", "verifying", "waiting")
            }
            if (index < 0) return 0
            operations[index] = operations[index].copy(manualPaused = true, phase = "paused", updatedAt = updatedAt)
            return 1
        }

        override suspend fun markSystemWaiting(updatedAt: Long): Int = synchronized(lock) {
            var changed = 0
            operations.indices.forEach { index ->
                val operation = operations[index]
                if (!operation.manualPaused && operation.phase in setOf("imported", "uploading", "verifying", "waiting")) {
                    operations[index] = operation.copy(phase = "waiting", updatedAt = updatedAt)
                    changed++
                }
            }
            return changed
        }

        override suspend fun recoverInterrupted(updatedAt: Long): Int = synchronized(lock) {
            var changed = 0
            operations.indices.forEach { index ->
                val operation = operations[index]
                if (!operation.manualPaused && operation.phase in setOf("uploading", "verifying")) {
                    operations[index] = operation.copy(phase = "waiting", updatedAt = updatedAt)
                    changed++
                }
            }
            return changed
        }

        override suspend fun markCompleted(id: String, revision: Long, remoteSha256: String, updatedAt: Long): Int = synchronized(lock) {
            val index = operations.indexOfFirst {
                it.id == id && it.revision == revision && it.phase == "uploading" &&
                    !it.manualPaused && it.sourceSha256 == remoteSha256
            }
            if (index < 0) return 0
            operations[index] = operations[index].copy(phase = "completed", remoteSha256 = remoteSha256, updatedAt = updatedAt)
            return 1
        }

        override suspend fun markFailed(id: String, revision: Long, error: String, updatedAt: Long): Int {
            currentCoroutineContext().ensureActive()
            return synchronized(lock) {
            val index = operations.indexOfFirst {
                it.id == id && it.revision == revision && it.phase == "uploading" && !it.manualPaused
            }
            if (index < 0) return 0
            operations[index] = operations[index].copy(phase = "failed", lastError = error, updatedAt = updatedAt)
            return 1
            }
        }
    }
}
