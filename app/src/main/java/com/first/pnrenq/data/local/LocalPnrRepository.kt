
package com.first.pnrenq.data.local

import com.first.pnrenq.domain.model.PnrInfo
import com.first.pnrenq.domain.validation.isValidPnr
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class LocalPnrRepository(
    private val dao: PnrDao
) {

    fun observeAll(): Flow<List<PnrInfo>> =
        dao.observeAll().map { records ->
            records.map { it.toDomain() }
        }

    fun observeByPnr(pnr: String): Flow<PnrInfo?> {
        require(isValidPnr(pnr)) {
            "Invalid PNR number"
        }

        return dao.observeByPnr(pnr).map {
            it?.toDomain()
        }
    }

    suspend fun getByPnr(pnr: String): PnrInfo? {
        require(isValidPnr(pnr)) {
            "Invalid PNR number"
        }

        return dao.getByPnr(pnr)?.toDomain()
    }

    suspend fun save(pnr: PnrInfo) {
        require(isValidPnr(pnr.pnrNumber)) {
            "PNR must contain exactly 10 digits"
        }

        val passengerIndices = pnr.passengers.map {
            it.index
        }

        require(passengerIndices.all { it > 0 }) {
            "Passenger indices must be positive"
        }

        require(passengerIndices.distinct().size ==
                passengerIndices.size) {
            "Duplicate passenger indices are not allowed"
        }

        dao.savePnr(
            pnr = pnr.toSavedEntity(),
            passengers = pnr.toPassengerEntities()
        )
    }

    suspend fun delete(pnr: String) {
        require(isValidPnr(pnr)) {
            "Invalid PNR number"
        }

        dao.deleteByPnr(pnr)
    }
}
