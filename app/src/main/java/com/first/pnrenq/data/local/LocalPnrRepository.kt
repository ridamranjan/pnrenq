package com.first.pnrenq.data.local

import com.first.pnrenq.domain.model.PnrInfo
import com.first.pnrenq.domain.validation.isValidPnr
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

internal data class SavedPnrReadResult(
    val pnrs: List<PnrInfo>,
    val unreadablePnrs: List<String>
)

class LocalPnrRepository(
    private val dao: PnrDao
) {

    fun observeAll(): Flow<List<PnrInfo>> =
        observeAllWithIssues()
            .map { it.pnrs }
            .distinctUntilChanged()

    internal fun observeAllWithIssues(): Flow<SavedPnrReadResult> =
        dao.observeAll().mapSavedSnapshots(
            keyOf = { it.pnr.pnrNumber },

            mapRow = { record ->
                record.pnr.pnrNumber to record.toDomainOrNull()
            },

            assemble = { rows ->
                val readable = ArrayList<PnrInfo>(rows.size)
                val unreadable = mutableListOf<String>()

                for ((number, pnr) in rows) {
                    if (pnr == null) {
                        unreadable.add(number)
                    } else {
                        readable.add(pnr)
                    }
                }

                SavedPnrReadResult(
                    readable,
                    unreadable
                )
            }
        ).flowOn(Dispatchers.Default)

    fun observeByPnr(pnr: String): Flow<PnrInfo?> {
        require(isValidPnr(pnr)) {
            "Invalid PNR number"
        }

        return dao.observeByPnr(pnr)
            .distinctUntilChanged()
            .map { it?.toDomainOrNull() }
            .distinctUntilChanged()
            .flowOn(Dispatchers.Default)
    }

    /** Null means absent or unreadable; re-adding a valid PNR can repair it. */
    suspend fun getByPnr(pnr: String): PnrInfo? {
        require(isValidPnr(pnr)) {
            "Invalid PNR number"
        }

        val record = dao.getByPnr(pnr) ?: return null

        return withContext(Dispatchers.Default) {
            record.toDomainOrNull()
        }
    }

    suspend fun save(pnr: PnrInfo) {
        validate(pnr)

        dao.savePnr(
            pnr = pnr.toSavedEntity(),
            passengers = pnr.toPassengerEntities()
        )
    }

    suspend fun saveIfExists(pnr: PnrInfo): Boolean {
        validate(pnr)

        return dao.savePnrIfExists(
            pnr = pnr.toSavedEntity(),
            passengers = pnr.toPassengerEntities()
        )
    }

    private fun validate(pnr: PnrInfo) {
        require(isValidPnr(pnr.pnrNumber)) {
            "PNR must contain exactly 10 digits"
        }

        val passengerIndices = pnr.passengers.map {
            it.index
        }

        require(passengerIndices.all { it > 0 }) {
            "Passenger indices must be positive"
        }

        require(
            passengerIndices.distinct().size ==
                    passengerIndices.size
        ) {
            "Duplicate passenger indices are not allowed"
        }
    }

    suspend fun delete(pnr: String) {
        require(isValidPnr(pnr)) {
            "Invalid PNR number"
        }

        dao.deleteByPnr(pnr)
    }
}