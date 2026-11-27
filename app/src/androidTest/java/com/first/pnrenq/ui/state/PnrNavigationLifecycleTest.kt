package com.first.pnrenq.ui.state

import android.app.Application
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewModelScope
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.first.pnrenq.data.local.SavedPnrReadResult
import com.first.pnrenq.domain.model.PnrInfo
import java.time.LocalDate
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.withTimeoutOrNull
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PnrNavigationLifecycleTest {
    @Test
    fun pendingEventsWaitUntilLifecycleResumes() = runBlocking {
        withContext(Dispatchers.Main.immediate) {
            val owner = object : LifecycleOwner {
                val registry = LifecycleRegistry(this)
                override val lifecycle: Lifecycle get() = registry
            }

            owner.registry.currentState = Lifecycle.State.STARTED

            val queue = PnrNavigationQueue()
            val received = Channel<PnrNavigationEvent>(Channel.UNLIMITED)

            val collector = launch(start = CoroutineStart.UNDISPATCHED) {
                owner.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                    queue.events.collect { received.send(it) }
                }
            }

            try {
                val first = PnrNavigationEvent.OpenDetails("1234567890", "request-1")

                queue.enqueue(first)

                assertNull(
                    withTimeoutOrNull(150L) {
                        received.receive()
                    }
                )

                owner.registry.currentState = Lifecycle.State.RESUMED

                assertEquals(
                    first,
                    withTimeout(5_000L) {
                        received.receive()
                    }
                )

                owner.registry.currentState = Lifecycle.State.STARTED

                val second = PnrNavigationEvent.ReturnAfterDelete("9876543210", "entry-2")

                queue.enqueue(second)

                assertNull(
                    withTimeoutOrNull(150L) {
                        received.receive()
                    }
                )

                owner.registry.currentState = Lifecycle.State.RESUMED

                assertEquals(
                    second,
                    withTimeout(5_000L) {
                        received.receive()
                    }
                )

                assertNull(
                    withTimeoutOrNull(150L) {
                        received.receive()
                    }
                )
            } finally {
                owner.registry.currentState = Lifecycle.State.DESTROYED
                collector.cancelAndJoin()
                received.close()
            }
        }
    }

    @Test
    fun viewModelPreservesRequestAndDeleteOriginInEvents() = runBlocking {
        val application = InstrumentationRegistry.getInstrumentation()
            .targetContext.applicationContext as Application

        withContext(Dispatchers.Main.immediate) {
            val pnr = "1234567890"

            val original = PnrInfo(
                pnrNumber = pnr,
                trainNumber = "12345",
                trainName = "Test Express",
                journeyDate = LocalDate.of(2026, 12, 1),
                fromStation = "ABC",
                toStation = "XYZ",
                bookingStatus = "WL 5",
                currentStatus = "CNF",
                chartStatus = null,
                passengers = emptyList(),
                lastCheckedAtMillis = 1000L
            )

            val records = MutableStateFlow(
                SavedPnrReadResult(
                    listOf(original),
                    emptyList()
                )
            )

            val repository = object : PnrViewModelDataSource {
                override fun observeAll() = records

                override suspend fun getByPnr(pnr: String) =
                    records.value.pnrs.firstOrNull {
                        it.pnrNumber == pnr
                    }

                override suspend fun save(pnr: PnrInfo) {
                    error("Unexpected write")
                }

                override suspend fun saveIfExists(pnr: PnrInfo): Boolean =
                    error("Unexpected refresh")

                override suspend fun delete(pnr: String) {
                    records.value = records.value.copy(
                        pnrs = records.value.pnrs.filterNot {
                            it.pnrNumber == pnr
                        }
                    )
                }
            }

            val vm = PnrViewModel(
                application,
                repository
            ) {
                error("Unexpected network request")
            }

            val received = Channel<PnrNavigationEvent>(Channel.UNLIMITED)

            val collector = launch(start = CoroutineStart.UNDISPATCHED) {
                vm.navigationEvents.collect {
                    received.send(it)
                }
            }

            try {
                vm.checkPnr(
                    pnr,
                    requestId = "request-1"
                )

                assertEquals(
                    PnrNavigationEvent.OpenDetails(pnr, "request-1"),
                    withTimeout(5_000L) {
                        received.receive()
                    }
                )

                vm.deletePnr(
                    pnr,
                    originEntryId = "entry-1"
                )

                assertEquals(
                    PnrNavigationEvent.ReturnAfterDelete(pnr, "entry-1"),
                    withTimeout(5_000L) {
                        received.receive()
                    }
                )

                assertNull(
                    repository.getByPnr(pnr)
                )
            } finally {
                collector.cancelAndJoin()
                received.close()
                vm.viewModelScope.cancel()
            }
        }
    }
}