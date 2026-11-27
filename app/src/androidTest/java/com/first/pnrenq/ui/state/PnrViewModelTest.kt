package com.first.pnrenq.ui.state

import android.app.Application
import androidx.lifecycle.viewModelScope
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.first.pnrenq.data.local.SavedPnrReadResult
import com.first.pnrenq.data.remote.PnrCheckException
import com.first.pnrenq.domain.model.PassengerStatus
import com.first.pnrenq.domain.model.PnrInfo
import java.time.LocalDate
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.withTimeoutOrNull
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PnrViewModelTest {

    private val a = "1234567890"
    private val b = "9876543210"

    @Test
    fun cachedPnrOpensWithoutNetworkRequest() = runBlocking {
        val original = sample(a)
        val fake = FakeRepository(listOf(original))
        withViewModel(fake, { error("Unexpected live request") }) { vm ->
            awaitState(vm) { !it.isLoading && it.pnrs == listOf(original) }
            onMain { vm.checkPnr(a) }
            // Collect only after completion: the event must still exist.
            assertEquals(PnrNavigationEvent.OpenDetails(a), nextEvent(vm))
            onMain { assertEquals(0, fake.saves) }
            assertFalse(vm.state.value.isChecking)
            assertNull(vm.state.value.addError)
        }
    }

    @Test
    fun duplicateCheckAndConflictingActionsAreBlockedWhileLookupRuns() = runBlocking {
        val entered = CompletableDeferred<Unit>()
        val release = CompletableDeferred<Unit>()
        var calls = 0
        val fake = FakeRepository()
        withViewModel(fake, { pnr ->
            calls++
            entered.complete(Unit)
            release.await()
            sample(pnr)
        }) { vm ->
            onMain { vm.checkPnr(a) }
            withTimeout(5_000L) { entered.await() }
            onMain {
                assertTrue(vm.state.value.isChecking)
                assertTrue(vm.state.value.isBusy(a))
                vm.checkPnr(a)
                vm.checkPnr(b)
                vm.refreshPnr(a)
                vm.deletePnr(a)
                assertEquals(1, calls)
                assertEquals(0, fake.deletes)
            }
            release.complete(Unit)
            assertEquals(PnrNavigationEvent.OpenDetails(a), nextEvent(vm))
            awaitState(vm) { !it.isChecking && it.pnrs == listOf(sample(a)) }
            onMain {
                assertEquals(1, fake.saves)
                assertEquals(0, fake.conditionalSaves)
                assertEquals(1, calls)
            }
        }
    }

    @Test
    fun invalidInputDoesNotStartLookup() = runBlocking {
        val fake = FakeRepository()
        withViewModel(fake, { error("Unexpected live request") }) { vm ->
            onMain { vm.checkPnr("123") }
            assertEquals("Enter a valid 10-digit PNR", vm.state.value.addError)
            assertFalse(vm.state.value.isChecking)
            onMain { assertEquals(0, fake.saves) }
        }
    }

    @Test
    fun failedLookupDoesNotSaveOrNavigate() = runBlocking {
        val fake = FakeRepository()
        withViewModel(fake, { throw PnrCheckException("Service unavailable") }) { vm ->
            onMain { vm.checkPnr(a) }
            awaitState(vm) { !it.isChecking && it.addError == "Service unavailable" }
            onMain { assertEquals(0, fake.saves) }
            assertFalse(vm.state.value.isBusy(a))
            assertNoEvent(vm)
        }
    }

    @Test
    fun failedSaveDoesNotNavigate() = runBlocking {
        val fake = FakeRepository().apply { failSave = true }
        withViewModel(fake, { sample(it) }) { vm ->
            onMain { vm.checkPnr(a) }
            awaitState(vm) { !it.isChecking && it.addError != null }
            assertEquals("Unable to save PNR. Please try again.", vm.state.value.addError)
            assertTrue(vm.state.value.pnrs.isEmpty())
            assertFalse(vm.state.value.isBusy(a))
            assertNoEvent(vm)
        }
    }

    @Test
    fun independentRefreshesRetainBothMessagesAndBlockSamePnrDeletion() = runBlocking {
        val fake = FakeRepository(listOf(sample(a), sample(b)))
        val gates = mapOf(a to CompletableDeferred<Unit>(), b to CompletableDeferred<Unit>())
        withViewModel(fake, { pnr ->
            gates.getValue(pnr).await()
            sample(pnr, "CNF")
        }) { vm ->
            onMain {
                vm.refreshPnr(a)
                vm.refreshPnr(b)
                vm.deletePnr(a)
                assertEquals(setOf(a, b), vm.state.value.refreshingPnrs)
                assertEquals(0, fake.deletes)
            }
            gates.getValue(b).complete(Unit)
            awaitState(vm) { b in it.operationMessages && !it.isBusy(b) }
            gates.getValue(a).complete(Unit)
            val state = awaitState(vm) { it.refreshingPnrs.isEmpty() && it.operationMessages.size == 2 }
            assertEquals(PnrMessageKind.REFRESH, state.operationMessages.getValue(a).kind)
            assertEquals("PNR status updated successfully.", state.operationMessages.getValue(b).text)
            onMain {
                vm.clearDetailsMessage() // Legacy UI must not erase scoped feedback.
                vm.clearOperationMessage(a, state.operationMessages.getValue(a).id)
                assertEquals(setOf(b), vm.state.value.operationMessages.keys)
                assertEquals(2, fake.conditionalSaves)
            }
        }
    }

    @Test
    fun staleAcknowledgementCannotClearNewerMessage() = runBlocking {
        val fake = FakeRepository(listOf(sample(a)))
        withViewModel(fake, { sample(it, "CNF") }) { vm ->
            onMain { vm.refreshPnr(a) }
            val first = awaitState(vm) { a in it.operationMessages }.operationMessages.getValue(a)
            onMain { vm.refreshPnr(a) }
            val second = awaitState(vm) {
                val id = it.operationMessages[a]?.id
                id != null && id != first.id
            }.operationMessages.getValue(a)
            onMain {
                vm.clearOperationMessage(a, first.id)
                assertEquals(second, vm.state.value.operationMessages[a])
                vm.clearOperationMessage(a, second.id)
                assertNull(vm.state.value.operationMessages[a])
            }
        }
    }

    @Test
    fun failedRefreshPreservesSavedInformation() = runBlocking {
        val original = sample(a)
        val fake = FakeRepository(listOf(original))
        withViewModel(fake, { throw PnrCheckException("Service unavailable") }) { vm ->
            awaitState(vm) { it.pnrs == listOf(original) }
            onMain { vm.refreshPnr(a) }
            val state = awaitState(vm) { a in it.operationMessages && !it.isBusy(a) }
            assertEquals(listOf(original), state.pnrs)
            assertEquals("Service unavailable", state.operationMessages.getValue(a).text)
            onMain { assertEquals(0, fake.conditionalSaves) }
        }
    }

    @Test
    fun refreshOfMissingRecordDoesNotInsertIt() = runBlocking {
        val fake = FakeRepository()
        withViewModel(fake, { sample(it) }) { vm ->
            onMain { vm.refreshPnr(a) }
            val state = awaitState(vm) { a in it.operationMessages }
            assertEquals("Saved PNR was not found.", state.operationMessages.getValue(a).text)
            onMain {
                assertEquals(1, fake.conditionalSaves)
                assertEquals(0, fake.saves)
                assertNull(fake.getByPnr(a))
            }
        }
    }

    @Test
    fun successfulDeleteEmitsEventAndOnlyRemovesSelectedRecord() = runBlocking {
        val fake = FakeRepository(listOf(sample(a), sample(b)))
        withViewModel(fake, { error("Unexpected live request") }) { vm ->
            onMain { vm.deletePnr(a) }
            assertEquals(PnrNavigationEvent.ReturnAfterDelete(a), nextEvent(vm))
            awaitState(vm) { !it.isBusy(a) && it.pnrs == listOf(sample(b)) }
            onMain { assertEquals(1, fake.deletes) }
        }
    }

    @Test
    fun failedDeleteRetainsRecordAndPublishesScopedError() = runBlocking {
        val fake = FakeRepository(listOf(sample(a))).apply { failDelete = true }
        withViewModel(fake, { error("Unexpected live request") }) { vm ->
            awaitState(vm) { it.pnrs == listOf(sample(a)) }
            onMain { vm.deletePnr(a) }
            val state = awaitState(vm) { a in it.operationMessages && !it.isBusy(a) }
            assertEquals(PnrMessageKind.DELETE, state.operationMessages.getValue(a).kind)
            assertEquals("Unable to delete PNR. Please try again.", state.operationMessages.getValue(a).text)
            assertEquals(listOf(sample(a)), state.pnrs)
            assertNoEvent(vm)
        }
    }

    @Test
    fun cancelledRefreshReleasesLockWithoutPublishingError() = runBlocking {
        val fake = FakeRepository(listOf(sample(a)))
        var calls = 0
        withViewModel(fake, { pnr ->
            calls++
            if (calls == 1) throw CancellationException("Synthetic cancellation")
            sample(pnr, "CNF")
        }) { vm ->
            onMain {
                vm.refreshPnr(a)
                assertFalse(vm.state.value.isBusy(a))
                assertNull(vm.state.value.operationMessages[a])
                assertEquals(0, fake.conditionalSaves)
                vm.refreshPnr(a)
            }
            awaitState(vm) { a in it.operationMessages && !it.isBusy(a) }
            onMain {
                assertEquals(2, calls)
                assertEquals(1, fake.conditionalSaves)
            }
        }
    }

    @Test
    fun cancellingViewModelScopeDuringCheckClearsBusyStateAndPreventsSave() = runBlocking {
        val entered = CompletableDeferred<Unit>()
        val gate = CompletableDeferred<Unit>()
        val fake = FakeRepository()
        withViewModel(fake, { pnr ->
            entered.complete(Unit)
            gate.await()
            sample(pnr)
        }) { vm ->
            onMain { vm.checkPnr(a) }
            withTimeout(5_000L) { entered.await() }
            onMain { vm.viewModelScope.cancel() }
            awaitState(vm) { !it.isChecking && !it.isBusy(a) }
            onMain {
                assertEquals(0, fake.saves)
                assertNull(vm.state.value.addError)
                // A cancelled scope must not leave a new operation marked busy.
                vm.checkPnr(a)
                assertFalse(vm.state.value.isChecking)
            }
            assertNoEvent(vm)
        }
    }

    @Test
    fun databaseFailurePreservesReadableRecordsAndRetryRecovers() = runBlocking {
        val original = sample(a)
        val fake = FakeRepository(listOf(original))
        withViewModel(fake, { error("Unexpected live request") }) { vm ->
            awaitState(vm) { it.pnrs == listOf(original) }
            onMain {
                fake.failObservation = true
                vm.loadSavedPnrs()
            }
            val failed = awaitState(vm) { it.loadError != null }
            assertEquals(listOf(original), failed.pnrs)
            assertFalse(failed.isLoading)
            onMain {
                fake.failObservation = false
                fake.reportUnreadable(listOf(b))
                vm.loadSavedPnrs()
            }
            val recovered = awaitState(vm) { it.unreadablePnrs == listOf(b) }
            assertEquals(listOf(original), recovered.pnrs)
            assertTrue(recovered.loadError!!.contains("…3210"))
            assertFalse(recovered.loadError!!.contains(b))
            onMain { fake.reportUnreadable(emptyList()) }
            awaitState(vm) { it.loadError == null && it.unreadablePnrs.isEmpty() }
        }
    }

    private suspend fun <T> onMain(block: suspend () -> T): T =
        withContext(Dispatchers.Main.immediate) { block() }

    private suspend fun withViewModel(
        fake: FakeRepository,
        lookup: suspend (String) -> PnrInfo,
        block: suspend (PnrViewModel) -> Unit
    ) {
        val application = InstrumentationRegistry.getInstrumentation()
            .targetContext.applicationContext as Application
        val vm = onMain { PnrViewModel(application, fake, lookup) }
        try {
            withTimeout(15_000L) { block(vm) }
        } finally {
            onMain { vm.viewModelScope.cancel() }
        }
    }

    private suspend fun awaitState(
        vm: PnrViewModel,
        predicate: (PnrUiState) -> Boolean
    ): PnrUiState = withTimeout(5_000L) { vm.state.first(predicate) }

    // first() checks pending delivery only. Queue acknowledgement is tested
    // separately with a direct, normally returning collector.
    private suspend fun nextEvent(vm: PnrViewModel): PnrNavigationEvent =
        withTimeout(5_000L) { vm.navigationEvents.first() }

    private suspend fun assertNoEvent(vm: PnrViewModel) {
        assertNull(withTimeoutOrNull(150L) { vm.navigationEvents.first() })
    }

    private fun sample(pnr: String, status: String = "WL 5") = PnrInfo(
        pnrNumber = pnr,
        trainNumber = "12345",
        trainName = "Test Express",
        journeyDate = LocalDate.of(2026, 12, 1),
        fromStation = "ABC",
        toStation = "XYZ",
        bookingStatus = "WL 10",
        currentStatus = status,
        chartStatus = null,
        passengers = listOf(PassengerStatus(1, "WL 10", status, null, null)),
        lastCheckedAtMillis = 1000L
    )

    private class FakeRepository(initial: List<PnrInfo> = emptyList()) : PnrViewModelDataSource {
        private val records = MutableStateFlow(SavedPnrReadResult(initial, emptyList()))
        var failObservation = false
        var failSave = false
        var failDelete = false
        var saves = 0
            private set
        var conditionalSaves = 0
            private set
        var deletes = 0
            private set

        override fun observeAll(): Flow<SavedPnrReadResult> = flow {
            if (failObservation) error("Synthetic database failure")
            emitAll(records)
        }

        override suspend fun getByPnr(pnr: String): PnrInfo? =
            records.value.pnrs.firstOrNull { it.pnrNumber == pnr }

        override suspend fun save(pnr: PnrInfo) {
            if (failSave) error("Synthetic write failure")
            saves++
            replace(pnr)
        }

        override suspend fun saveIfExists(pnr: PnrInfo): Boolean {
            conditionalSaves++
            if (getByPnr(pnr.pnrNumber) == null) return false
            replace(pnr)
            return true
        }

        override suspend fun delete(pnr: String) {
            if (failDelete) error("Synthetic delete failure")
            deletes++
            records.value = records.value.copy(
                pnrs = records.value.pnrs.filterNot { it.pnrNumber == pnr }
            )
        }

        fun reportUnreadable(pnrs: List<String>) {
            records.value = records.value.copy(unreadablePnrs = pnrs)
        }

        private fun replace(pnr: PnrInfo) {
            records.value = records.value.copy(
                pnrs = records.value.pnrs.filterNot { it.pnrNumber == pnr.pnrNumber } + pnr
            )
        }
    }
}