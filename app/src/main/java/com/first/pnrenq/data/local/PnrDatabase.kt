
package com.first.pnrenq.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        SavedPnrEntity::class,
        PassengerEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class PnrDatabase : RoomDatabase() {

    abstract fun pnrDao(): PnrDao
}
