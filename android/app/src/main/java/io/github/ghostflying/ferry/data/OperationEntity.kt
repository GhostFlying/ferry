package io.github.ghostflying.ferry.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "operations")
data class OperationEntity(
    @PrimaryKey val id: String,
    val revision: Long,
    val phase: String,
    val manualPaused: Boolean,
    val sourcePath: String,
    val privateCopy: String,
    val sourceSha256: String,
    val remoteSha256: String?,
    val lastError: String?,
    val updatedAt: Long,
)
