package io.github.ghostflying.ferry.data

import java.time.Clock
import java.util.UUID

class OperationRepository(
    private val dao: OperationDao,
    private val clock: Clock = Clock.systemUTC(),
) {
    suspend fun createIntent(
        revision: Long,
        sourcePath: String,
        privateCopy: String,
    ): OperationEntity {
        val operation = OperationEntity(
            id = UUID.randomUUID().toString(),
            revision = revision,
            phase = "imported",
            manualPaused = false,
            sourcePath = sourcePath,
            privateCopy = privateCopy,
            sourceSha256 = "",
            remoteSha256 = null,
            lastError = null,
            updatedAt = clock.millis(),
        )
        dao.save(operation)
        return operation
    }

    suspend fun pause(id: String) {
        val current = dao.find(id) ?: return
        dao.setPause(id, current.revision, paused = true, phase = "paused", updatedAt = clock.millis())
    }

    suspend fun resumeFromForeground(id: String) {
        val current = dao.find(id) ?: return
        if (!current.manualPaused && current.phase == "waiting") {
            dao.setPause(id, current.revision, paused = false, phase = "waiting", updatedAt = clock.millis())
        }
    }
}
