package com.first.pnrenq.data.local

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object PnrDatabaseProvider {

    @Volatile
    private var instance: PnrDatabase? = null

    internal val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                ALTER TABLE saved_pnrs
                ADD COLUMN detailsJson TEXT
                """.trimIndent()
            )
        }
    }

    fun get(context: Context): PnrDatabase {
        return instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(
                context.applicationContext,
                PnrDatabase::class.java,
                "pnr_enq.db"
            )
                .addMigrations(MIGRATION_1_2)
                .build()
                .also {
                    instance = it
                }
        }
    }
}