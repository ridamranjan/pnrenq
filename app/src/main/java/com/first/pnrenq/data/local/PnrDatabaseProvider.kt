
package com.first.pnrenq.data.local

import android.content.Context
import androidx.room.Room

object PnrDatabaseProvider {

    @Volatile
    private var instance: PnrDatabase? = null

    fun get(context: Context): PnrDatabase {
        return instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(
                context.applicationContext,
                PnrDatabase::class.java,
                "pnr_enq.db"
            ).build().also {
                instance = it
            }
        }
    }
}
