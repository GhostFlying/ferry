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
import io.github.ghostflying.ferry.source.ImportSummary
import io.github.ghostflying.ferry.source.SourceStatus
import org.junit.Test

class FerryUiStateTest {
    @Test
    fun configurationStartsUnconfiguredAndFailedStateIsClassified() {
        assertEquals("尚未配置", UiConfigurationSnapshot().sourceLabel)
        assertEquals(ConfigurationScreenModel("目标", "尚未配置", "SMB 配置接口待接入"), configurationScreenModel(FerryTab.TARGETS))
        assertEquals(ConfigurationScreenModel("规则", "尚未配置", "规则编辑接口待接入"), configurationScreenModel(FerryTab.RULES))
        assertEquals(FerryTab.TASKS, FerryUiState().tab)
        assertEquals(listOf(FerryTab.TASKS, FerryTab.SOURCES, FerryTab.TARGETS, FerryTab.RULES), FerryTab.entries)
        assertFalse(FerryUnsupportedActions.canResume)
        assertFalse(FerryUnsupportedActions.canRetry)
        assertFalse(FerryUnsupportedActions.canRecheckSpace)
        assertFalse(FerryUnsupportedActions.canSaveConfiguration)
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
    fun operationLabelsCoverAcceptedTaskStatesWithoutFakeProgress() {
        assertTrue(operationLabel(operation("one", phase = "imported")).startsWith("待上传"))
        assertTrue(operationLabel(operation("one", phase = "waiting")).startsWith("等待前台运行"))
        assertTrue(operationLabel(operation("one", phase = "uploading")).startsWith("正在上传"))
        assertTrue(operationLabel(operation("one", phase = "verifying")).contains("内容校验中"))
        assertFalse(operationLabel(operation("one", phase = "verifying")).contains("GB"))
        assertTrue(operationLabel(operation("one", phase = "completed")).startsWith("已完成"))
        assertTrue(operationLabel(operation("one", phase = "paused").copy(manualPaused = true)).startsWith("用户暂停"))
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

    @Test
    fun lifecycleStartStopKeepsWorkForegroundOnly() = runBlocking {
        val dao = FakeUiDao(operation("one", phase = "imported"))
        val repository = OperationRepository(dao)
        val coordinator = ForegroundExecutionCoordinator(
            repository,
            CoroutineScope(SupervisorJob() + Dispatchers.Default),
            upload = null,
        )
        val controller = FerryUiController(repository, coordinator)

        controller.onStart()
        assertEquals("imported", controller.state.value.operations.single().phase)
        controller.onStop()

        assertEquals("waiting", controller.state.value.operations.single().phase)
    }

    @Test
    fun sourceScreenShowsAcceptedReselectWarningForPocketHint() {
        assertEquals(SourceScreenModel("尚未选择目录", "等待授权", false, "选择目录"), sourceScreenModel(null, SourceStatus.NotConfigured))
        assertTrue(sourceScreenModel("DCIM", SourceStatus.NeedsReselect).showsReselectWarning)
        assertTrue(sourceScreenModel("DCIM", SourceStatus.PocketNeedsOtg).showsReselectWarning)
        assertEquals(
            "已导入 3 · 已有 2 · 未完成 1",
            sourceScreenModel("DCIM", SourceStatus.Ready(ImportSummary(imported = 2, skipped = 2, registered = 1, failed = 1))).status,
        )
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
        override suspend fun findBySourcePath(sourcePath: String): OperationEntity? = operations.find { it.sourcePath == sourcePath }
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
        override suspend fun markSystemWaiting(updatedAt: Long): Int {
            var changed = 0
            operations.indices.forEach { index ->
                val current = operations[index]
                if (!current.manualPaused && current.phase in setOf("imported", "uploading", "verifying", "waiting")) {
                    operations[index] = current.copy(phase = "waiting", updatedAt = updatedAt)
                    changed++
                }
            }
            return changed
        }
        override suspend fun recoverInterrupted(updatedAt: Long): Int {
            var changed = 0
            operations.indices.forEach { index ->
                val current = operations[index]
                if (!current.manualPaused && current.phase in setOf("uploading", "verifying")) {
                    operations[index] = current.copy(phase = "waiting", updatedAt = updatedAt)
                    changed++
                }
            }
            return changed
        }
        override suspend fun markCompleted(id: String, revision: Long, remoteSha256: String, updatedAt: Long): Int = 0
        override suspend fun markFailed(id: String, revision: Long, error: String, updatedAt: Long): Int = 0
    }
}
