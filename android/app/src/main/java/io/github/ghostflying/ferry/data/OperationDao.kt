package io.github.ghostflying.ferry.data

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert

@Dao
interface OperationDao {
    @Query("SELECT * FROM operations WHERE id = :id")
    suspend fun find(id: String): OperationEntity?

    @Upsert
    suspend fun save(operation: OperationEntity)

    @Query("UPDATE operations SET manualPaused = :paused, phase = :phase, updatedAt = :updatedAt WHERE id = :id AND revision = :revision AND phase IN ('imported', 'uploading', 'verifying', 'waiting', 'paused')")
    suspend fun setPause(id: String, revision: Long, paused: Boolean, phase: String, updatedAt: Long): Int
}
