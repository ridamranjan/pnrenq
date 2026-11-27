package com.first.pnrenq.ui.state

import com.first.pnrenq.data.local.LocalPnrRepository
import com.first.pnrenq.data.local.SavedPnrReadResult
import com.first.pnrenq.domain.model.PnrInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

/** Small boundary for testing ViewModel workflows without a database or backend. */
internal interface PnrViewModelDataSource {
    fun observeAll(): Flow<SavedPnrReadResult>
    suspend fun getByPnr(pnr: String): PnrInfo?
    suspend fun save(pnr: PnrInfo)
    suspend fun saveIfExists(pnr: PnrInfo): Boolean
    suspend fun delete(pnr: String)
}

internal class LocalPnrViewModelDataSource(
    private val repository: LocalPnrRepository
) : PnrViewModelDataSource {
    override fun observeAll(): Flow<SavedPnrReadResult> =
        repository.observeAllWithIssues()

    override suspend fun getByPnr(pnr: String): PnrInfo? =
        repository.getByPnr(pnr)

    override suspend fun save(pnr: PnrInfo) =
        withContext(Dispatchers.IO) { repository.save(pnr) }

    override suspend fun saveIfExists(pnr: PnrInfo): Boolean =
        withContext(Dispatchers.IO) { repository.saveIfExists(pnr) }

    override suspend fun delete(pnr: String) =
        withContext(Dispatchers.IO) { repository.delete(pnr) }
}