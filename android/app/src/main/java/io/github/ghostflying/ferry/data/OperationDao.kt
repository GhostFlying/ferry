package io.github.ghostflying.ferry.data

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert

@Dao
interface OperationDao {
    @Query("SELECT * FROM operations WHERE id = :id")
    suspend fun find(id: String): OperationEntity?

    @Query("SELECT * FROM operations ORDER BY updatedAt DESC")
    suspend fun findAll(): List<OperationEntity>

    @Query("SELECT * FROM operations WHERE phase IN ('imported', 'waiting') AND manualPaused = 0 AND sourceSha256 <> '' AND privateCopy <> '' ORDER BY updatedAt ASC")
    suspend fun findEligible(): List<OperationEntity>

    @Upsert
    suspend fun save(operation: OperationEntity)

    @Query("UPDATE operations SET phase = 'uploading', updatedAt = :updatedAt WHERE id = :id AND revision = :revision AND phase = :expectedPhase AND manualPaused = 0 AND sourceSha256 <> '' AND privateCopy <> ''")
    suspend fun claimForUpload(id: String, revision: Long, expectedPhase: String, updatedAt: Long): Int

    @Query("UPDATE operations SET manualPaused = 1, phase = 'paused', updatedAt = :updatedAt WHERE id = :id AND revision = :revision AND manualPaused = 0 AND phase IN ('imported', 'uploading', 'verifying', 'waiting')")
    suspend fun setManualPause(id: String, revision: Long, updatedAt: Long): Int

    @Query("UPDATE operations SET phase = 'waiting', updatedAt = :updatedAt WHERE manualPaused = 0 AND phase IN ('imported', 'uploading', 'verifying', 'waiting')")
    suspend fun markSystemWaiting(updatedAt: Long): Int

    @Query("UPDATE operations SET phase = 'waiting', updatedAt = :updatedAt WHERE manualPaused = 0 AND phase IN ('uploading', 'verifying')")
    suspend fun recoverInterrupted(updatedAt: Long): Int

    @Query("UPDATE operations SET phase = 'completed', remoteSha256 = :remoteSha256, lastError = NULL, updatedAt = :updatedAt WHERE id = :id AND revision = :revision AND phase = 'uploading' AND manualPaused = 0 AND sourceSha256 = :remoteSha256")
    suspend fun markCompleted(id: String, revision: Long, remoteSha256: String, updatedAt: Long): Int

    @Query("UPDATE operations SET phase = 'failed', lastError = :error, updatedAt = :updatedAt WHERE id = :id AND revision = :revision AND phase = 'uploading' AND manualPaused = 0")
    suspend fun markFailed(id: String, revision: Long, error: String, updatedAt: Long): Int
}
