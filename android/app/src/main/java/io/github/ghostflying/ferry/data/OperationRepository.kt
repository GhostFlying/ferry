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
        dao.setManualPause(id, current.revision, updatedAt = clock.millis())
    }

    suspend fun eligibleOperations(): List<OperationEntity> = dao.findEligible()

    suspend fun claimForUpload(operation: OperationEntity): Boolean =
        dao.claimForUpload(
            id = operation.id,
            revision = operation.revision,
            expectedPhase = operation.phase,
            updatedAt = clock.millis(),
        ) == 1

    suspend fun markSystemWaiting(): Int = dao.markSystemWaiting(clock.millis())

    suspend fun markCompleted(operation: OperationEntity, remoteSha256: String): Boolean =
        dao.markCompleted(operation.id, operation.revision, remoteSha256.lowercase(), clock.millis()) == 1

    suspend fun markFailed(operation: OperationEntity, error: String): Boolean =
        dao.markFailed(operation.id, operation.revision, error, clock.millis()) == 1

    suspend fun isManuallyPaused(id: String): Boolean = dao.find(id)?.manualPaused == true

    suspend fun current(id: String): OperationEntity? = dao.find(id)

    suspend fun pauseAndReport(id: String): Boolean {
        val current = dao.find(id) ?: return false
        return dao.setManualPause(id, current.revision, updatedAt = clock.millis()) == 1
    }
}
