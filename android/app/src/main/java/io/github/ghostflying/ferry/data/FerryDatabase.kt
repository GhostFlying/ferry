package io.github.ghostflying.ferry.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [OperationEntity::class], version = 1, exportSchema = true)
abstract class FerryDatabase : RoomDatabase() {
    abstract fun operationDao(): OperationDao
}
