
package com.first.pnrenq.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

data class PnrWithPassengers(
    @androidx.room.Embedded
    val pnr: SavedPnrEntity,

    @androidx.room.Relation(
        parentColumn = "pnrNumber",
        entityColumn = "pnrNumber"
    )
    val passengers: List<PassengerEntity>
)

@Dao
abstract class PnrDao {

    @Transaction
    @Query("""
        SELECT * FROM saved_pnrs
        ORDER BY lastCheckedAtMillis DESC
    """)
    abstract fun observeAll(): Flow<List<PnrWithPassengers>>

    @Transaction
    @Query("""
        SELECT * FROM saved_pnrs
        WHERE pnrNumber = :pnr
        LIMIT 1
    """)
    abstract fun observeByPnr(
        pnr: String
    ): Flow<PnrWithPassengers?>

    @Transaction
    @Query("""
        SELECT * FROM saved_pnrs
        WHERE pnrNumber = :pnr
        LIMIT 1
    """)
    abstract suspend fun getByPnr(
        pnr: String
    ): PnrWithPassengers?

    @Query("""
        SELECT createdAtMillis FROM saved_pnrs
        WHERE pnrNumber = :pnr
        LIMIT 1
    """)
    abstract suspend fun getCreatedAt(
        pnr: String
    ): Long?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    protected abstract suspend fun insertPnr(
        pnr: SavedPnrEntity
    )

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    protected abstract suspend fun insertPassengers(
        passengers: List<PassengerEntity>
    )

    @Query("""
        DELETE FROM passengers
        WHERE pnrNumber = :pnr
    """)
    protected abstract suspend fun deletePassengers(
        pnr: String
    )

    @Query("""
        DELETE FROM saved_pnrs
        WHERE pnrNumber = :pnr
    """)
    abstract suspend fun deleteByPnr(
        pnr: String
    )

    @Transaction
    open suspend fun savePnr(
        pnr: SavedPnrEntity,
        passengers: List<PassengerEntity>
    ) {
        val originalCreatedAt =
            getCreatedAt(pnr.pnrNumber)

        val updatedPnr = pnr.copy(
            createdAtMillis =
                originalCreatedAt ?: pnr.createdAtMillis
        )

        deletePassengers(pnr.pnrNumber)
        insertPnr(updatedPnr)
        insertPassengers(passengers)
    }
}
