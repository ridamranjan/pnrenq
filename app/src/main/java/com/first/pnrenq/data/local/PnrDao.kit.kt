package com.first.pnrenq.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.first.pnrenq.domain.validation.isValidPnr
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

    @Insert(onConflict = OnConflictStrategy.ABORT)
    protected abstract suspend fun insertPnr(
        pnr: SavedPnrEntity
    )

    @Update(onConflict = OnConflictStrategy.ABORT)
    protected abstract suspend fun updatePnr(
        pnr: SavedPnrEntity
    ): Int

    @Insert(onConflict = OnConflictStrategy.ABORT)
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
        validateSnapshot(pnr, passengers)

        val originalCreatedAt =
            getCreatedAt(pnr.pnrNumber)

        writeSnapshot(
            pnr,
            passengers,
            originalCreatedAt
        )
    }

    /** Never insert a record that was deleted before this transaction. */
    @Transaction
    open suspend fun savePnrIfExists(
        pnr: SavedPnrEntity,
        passengers: List<PassengerEntity>
    ): Boolean {
        validateSnapshot(pnr, passengers)

        val originalCreatedAt =
            getCreatedAt(pnr.pnrNumber) ?: return false

        writeSnapshot(
            pnr,
            passengers,
            originalCreatedAt
        )

        return true
    }

    private fun validateSnapshot(
        pnr: SavedPnrEntity,
        passengers: List<PassengerEntity>
    ) {
        require(isValidPnr(pnr.pnrNumber)) {
            "Invalid PNR number"
        }

        require(
            passengers.all {
                it.pnrNumber == pnr.pnrNumber
            }
        ) {
            "All passengers must belong to the supplied PNR"
        }

        require(
            passengers.all {
                it.passengerIndex > 0
            }
        ) {
            "Passenger indices must be positive"
        }

        require(
            passengers.map { it.passengerIndex }
                .distinct().size == passengers.size
        ) {
            "Duplicate passenger indices are not allowed"
        }
    }

    // Only called by the two transactional entry points above.
    private suspend fun writeSnapshot(
        pnr: SavedPnrEntity,
        passengers: List<PassengerEntity>,
        originalCreatedAt: Long?
    ) {
        if (originalCreatedAt == null) {
            insertPnr(pnr)
        } else {
            check(
                updatePnr(
                    pnr.copy(
                        createdAtMillis = originalCreatedAt
                    )
                ) == 1
            )
        }

        deletePassengers(pnr.pnrNumber)
        insertPassengers(passengers)
    }
}