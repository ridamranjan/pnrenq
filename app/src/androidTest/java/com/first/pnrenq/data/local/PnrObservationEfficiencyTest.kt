package com.first.pnrenq.data.local

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.first.pnrenq.domain.model.PassengerStatus
import com.first.pnrenq.domain.model.PnrDetails
import com.first.pnrenq.domain.model.PnrInfo
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PnrObservationEfficiencyTest {

    @Test
    fun unchangedRecordsAreReusedAndJsonChangesRepairAndDeletionAreVisible() =
        runBlocking {
            val a = sample("0123456789")
            val b = sample("9876543210")

            val aRow = row(a)
            val bRow = row(b)

            // Change only supplementary data, not the relational passenger values.
            val updated = b.copy(
                details = b.details.copy(
                    bookingFare = 500.50
                ),

                passengers = b.passengers.map {
                    it.copy(
                        currentBerthCode = "UB"
                    )
                }
            )

            val updatedRow = row(updated)

            val metadataOnly = updatedRow.copy(
                pnr = updatedRow.pnr.copy(
                    createdAtMillis = 456L
                )
            )

            val damaged = metadataOnly.copy(
                pnr = metadataOnly.pnr.copy(
                    journeyDate = "2026-02-31"
                )
            )

            val repository = LocalPnrRepository(
                ScriptedDao(
                    listOf(
                        listOf(aRow, bRow),

                        listOf(
                            aRow.copy(),
                            bRow.copy()
                        ),

                        listOf(aRow, updatedRow),

                        listOf(aRow, metadataOnly),

                        listOf(aRow, damaged),

                        listOf(aRow, updatedRow),

                        listOf(aRow),

                        listOf(aRow, updatedRow)
                    )
                )
            )

            val results =
                repository.observeAllWithIssues()
                    .toList()

            assertEquals(
                listOf(
                    SavedPnrReadResult(
                        listOf(a, b),
                        emptyList()
                    ),

                    SavedPnrReadResult(
                        listOf(a, updated),
                        emptyList()
                    ),

                    SavedPnrReadResult(
                        listOf(a),
                        listOf(b.pnrNumber)
                    ),

                    SavedPnrReadResult(
                        listOf(a, updated),
                        emptyList()
                    ),

                    SavedPnrReadResult(
                        listOf(a),
                        emptyList()
                    ),

                    SavedPnrReadResult(
                        listOf(a, updated),
                        emptyList()
                    )
                ),

                results
            )

            // A is unchanged throughout. A repeated decode would create a new model.
            val first =
                results.first().pnrs.first()

            assertTrue(
                results.all {
                    it.pnrs.first() === first
                }
            )

            assertNotSame(
                results[0].pnrs[1],
                results[1].pnrs[1]
            )

            // After deletion, even identical raw data must be mapped again.
            assertNotSame(
                results[3].pnrs[1],
                results[5].pnrs[1]
            )
        }

    @Test
    fun singleRecordObservationSuppressesEqualModelsButRetainsNullTransitions() =
        runBlocking {
            val original =
                sample("0123456789")

            val valid = row(original)

            val metadataOnly = valid.copy(
                pnr = valid.pnr.copy(
                    createdAtMillis = 456L
                )
            )

            val damaged = valid.copy(
                pnr = valid.pnr.copy(
                    journeyDate = "invalid"
                )
            )

            val repository = LocalPnrRepository(
                ScriptedDao(
                    listOf(
                        emptyList(),
                        listOf(valid),
                        listOf(valid.copy()),
                        listOf(metadataOnly),
                        listOf(damaged),
                        listOf(damaged),
                        emptyList(),
                        listOf(valid),
                        emptyList()
                    )
                )
            )

            assertEquals(
                listOf(
                    null,
                    original,
                    null,
                    original,
                    null
                ),

                repository.observeByPnr(
                    original.pnrNumber
                ).toList()
            )
        }

    private fun row(
        pnr: PnrInfo
    ) = PnrWithPassengers(
        pnr.toSavedEntity(
            createdAtMillis = 123L
        ),

        pnr.toPassengerEntities()
    )

    private fun sample(
        number: String
    ) = PnrInfo(
        pnrNumber = number,
        trainNumber = "12345",
        trainName = "Test Express",
        journeyDate = LocalDate.of(2026, 12, 25),
        fromStation = "ABC",
        toStation = "XYZ",
        bookingStatus = "WL 5",
        currentStatus = "CNF",
        chartStatus = null,

        passengers = listOf(
            PassengerStatus(
                1,
                "WL 5",
                "CNF",
                "B2",
                "35",
                currentStatusCode = "CNF",
                currentBerthNo = 35,
                currentBerthCode = "LB"
            )
        ),

        lastCheckedAtMillis = 1000L,

        details = PnrDetails(
            bookingFare = 125.25
        )
    )

    /** Finite scripted emissions avoid relying on Room invalidation timing. */
    private class ScriptedDao(
        private val snapshots: List<List<PnrWithPassengers>>
    ) : PnrDao() {

        override fun observeAll():
                Flow<List<PnrWithPassengers>> =
            snapshots.asFlow()

        override fun observeByPnr(
            pnr: String
        ): Flow<PnrWithPassengers?> =
            observeAll().map { rows ->
                rows.firstOrNull {
                    it.pnr.pnrNumber == pnr
                }
            }

        override suspend fun getByPnr(
            pnr: String
        ): PnrWithPassengers? =
            error("Unexpected read")

        override suspend fun getCreatedAt(
            pnr: String
        ): Long? =
            error("Unexpected read")

        protected override suspend fun insertPnr(
            pnr: SavedPnrEntity
        ): Unit =
            error("Unexpected write")

        protected override suspend fun updatePnr(
            pnr: SavedPnrEntity
        ): Int =
            error("Unexpected write")

        protected override suspend fun insertPassengers(
            passengers: List<PassengerEntity>
        ): Unit =
            error("Unexpected write")

        protected override suspend fun deletePassengers(
            pnr: String
        ): Unit =
            error("Unexpected write")

        override suspend fun deleteByPnr(
            pnr: String
        ): Unit =
            error("Unexpected write")
    }
}