package io.github.ghostflying.ferry.ui

import io.github.ghostflying.ferry.data.OperationDao
import io.github.ghostflying.ferry.data.OperationEntity
import io.github.ghostflying.ferry.data.OperationRepository
import io.github.ghostflying.ferry.lifecycle.ForegroundExecutionCoordinator
import java.util.Collections
import java.util.concurrent.atomic.AtomicInteger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FerryUiStateTest {
    @Test
    fun configurationStartsUnconfiguredAndFailedStateIsClassified() {
        assertEquals("尚未配置", UiConfigurationSnapshot().sourceLabel)
        assertTrue(isVerificationFailure("remote readback SHA-256 mismatch"))
        assertFalse(isVerificationFailure("network timeout"))
        assertTrue(operationLabel(operation("one", phase = "failed", error = "network timeout"))
            .startsWith("任务未完成"))
        assertTrue(operationLabel(operation("one", phase = "failed", error = "readback mismatch"))
            .startsWith("校验失败"))
    }

    @Test
    fun reloadReadsPhaseTransitionsFromRepositorySnapshot() = runBlocking {
        val dao = FakeUiDao(operation("one", phase = "waiting"))
        val repository = OperationRepository(dao)
        val coordinator = ForegroundExecutionCoordinator(
            repository,
            CoroutineScope(SupervisorJob() + Dispatchers.Default),
        ) { "a".repeat(64) }
        val controller = FerryUiController(repository, coordinator)

        controller.reload()
        assertEquals("waiting", controller.state.value.operations.single().phase)
        dao.operations[0] = dao.operations[0].copy(phase = "completed", remoteSha256 = "a".repeat(64))
        controller.reload()

        assertEquals("completed", controller.state.value.operations.single().phase)
        coordinator.onStop()
    }

    @Test
    fun pauseActionPersistsManualPauseAndReloadsState() = runBlocking {
        val dao = FakeUiDao(operation("one", phase = "waiting"))
        val repository = OperationRepository(dao)
        val coordinator = ForegroundExecutionCoordinator(
            repository,
            CoroutineScope(SupervisorJob() + Dispatchers.Default),
        ) { "a".repeat(64) }
        val controller = FerryUiController(repository, coordinator)

        controller.reload()
        controller.pause("one")

        assertTrue(controller.state.value.operations.single().manualPaused)
        assertEquals("paused", controller.state.value.operations.single().phase)
        coordinator.onStop()
    }

    private fun operation(id: String, phase: String, error: String? = null) = OperationEntity(
        id = id,
        revision = 1,
        phase = phase,
        manualPaused = false,
        sourcePath = "DCIM/$id.mp4",
        privateCopy = "/spool/$id",
        sourceSha256 = "a".repeat(64),
        remoteSha256 = null,
        lastError = error,
        updatedAt = 1_000,
    )

    private class FakeUiDao(initial: OperationEntity) : OperationDao {
        val operations = Collections.synchronizedList(mutableListOf(initial))
        private val calls = AtomicInteger()

        override suspend fun find(id: String): OperationEntity? = operations.find { it.id == id }
        override suspend fun findAll(): List<OperationEntity> = operations.toList()
        override suspend fun findEligible(): List<OperationEntity> = operations.filter {
            it.phase in setOf("imported", "waiting") && !it.manualPaused && it.sourceSha256.isNotEmpty() && it.privateCopy.isNotEmpty()
        }
        override suspend fun save(operation: OperationEntity) {
            operations.removeAll { it.id == operation.id }
            operations += operation
        }
        override suspend fun claimForUpload(id: String, revision: Long, expectedPhase: String, updatedAt: Long): Int {
            val index = operations.indexOfFirst { it.id == id && it.revision == revision && it.phase == expectedPhase && !it.manualPaused }
            if (index < 0) return 0
            operations[index] = operations[index].copy(phase = "uploading", updatedAt = updatedAt)
            calls.incrementAndGet()
            return 1
        }
        override suspend fun setManualPause(id: String, revision: Long, updatedAt: Long): Int {
            val index = operations.indexOfFirst { it.id == id && it.revision == revision && !it.manualPaused && it.phase in setOf("imported", "uploading", "verifying", "waiting") }
            if (index < 0) return 0
            operations[index] = operations[index].copy(manualPaused = true, phase = "paused", updatedAt = updatedAt)
            return 1
        }
        override suspend fun markSystemWaiting(updatedAt: Long): Int = 0
        override suspend fun markCompleted(id: String, revision: Long, remoteSha256: String, updatedAt: Long): Int = 0
        override suspend fun markFailed(id: String, revision: Long, error: String, updatedAt: Long): Int = 0
    }
}
