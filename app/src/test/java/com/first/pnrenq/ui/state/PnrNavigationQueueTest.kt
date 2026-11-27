package com.first.pnrenq.ui.state

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PnrNavigationQueueTest {

    @Test
    fun queuedEventsAreDeliveredInOrderAfterCollectorGap() = runBlocking {
        val queue = PnrNavigationQueue()
        val first = PnrNavigationEvent.OpenDetails("1234567890")
        val second = PnrNavigationEvent.ReturnAfterDelete("9876543210")
        queue.enqueue(first)
        queue.enqueue(second)

        val received = mutableListOf<PnrNavigationEvent>()
        val delivered = CompletableDeferred<Unit>()
        val collector = launch(start = CoroutineStart.UNDISPATCHED) {
            queue.events.collect {
                received.add(it)
                if (received.size == 2) delivered.complete(Unit)
            }
        }
        try {
            withTimeout(5_000L) { delivered.await() }
            assertEquals(listOf(first, second), received)
        } finally {
            collector.cancelAndJoin()
        }
    }

    @Test
    fun completedHandlerIsNotReplayedToNewCollector() = runBlocking {
        val queue = PnrNavigationQueue()
        val event = PnrNavigationEvent.OpenDetails("1234567890")
        queue.enqueue(event)
        val received = mutableListOf<PnrNavigationEvent>()
        val firstCollector = launch(start = CoroutineStart.UNDISPATCHED) {
            queue.events.collect { received.add(it) }
        }
        firstCollector.cancelAndJoin()
        assertEquals(listOf(event), received)

        val secondCollector = launch(start = CoroutineStart.UNDISPATCHED) {
            queue.events.collect { received.add(it) }
        }
        secondCollector.cancelAndJoin()
        assertEquals(listOf(event), received)
    }

    @Test
    fun cancelledHandlerLeavesEventForReplacementCollector() = runBlocking {
        val queue = PnrNavigationQueue()
        val event = PnrNavigationEvent.OpenDetails("1234567890")
        queue.enqueue(event)
        val entered = CompletableDeferred<Unit>()
        val firstCollector = launch(start = CoroutineStart.UNDISPATCHED) {
            queue.events.collect {
                entered.complete(Unit)
                awaitCancellation()
            }
        }
        try {
            withTimeout(5_000L) { entered.await() }
        } finally {
            firstCollector.cancelAndJoin()
        }

        val received = mutableListOf<PnrNavigationEvent>()
        val replacement = launch(start = CoroutineStart.UNDISPATCHED) {
            queue.events.collect { received.add(it) }
        }
        replacement.cancelAndJoin()
        assertEquals(listOf(event), received)
    }

    @Test
    fun identicalEventsAreNotConflated() = runBlocking {
        val queue = PnrNavigationQueue()
        val event = PnrNavigationEvent.ReturnAfterDelete("1234567890")
        queue.enqueue(event)
        queue.enqueue(event)
        val received = mutableListOf<PnrNavigationEvent>()
        val delivered = CompletableDeferred<Unit>()
        val collector = launch(start = CoroutineStart.UNDISPATCHED) {
            queue.events.collect {
                received.add(it)
                if (received.size == 2) delivered.complete(Unit)
            }
        }
        try {
            withTimeout(5_000L) { delivered.await() }
            assertEquals(listOf(event, event), received)
        } finally {
            collector.cancelAndJoin()
        }
    }

    @Test
    fun overlappingCollectorsDoNotHandleSameEventConcurrently() = runBlocking {
        val queue = PnrNavigationQueue()
        queue.enqueue(PnrNavigationEvent.OpenDetails("1234567890"))
        val entered = CompletableDeferred<Unit>()
        val first = launch(start = CoroutineStart.UNDISPATCHED) {
            queue.events.collect {
                entered.complete(Unit)
                awaitCancellation()
            }
        }
        val received = mutableListOf<PnrNavigationEvent>()
        val delivered = CompletableDeferred<Unit>()
        val second = launch(start = CoroutineStart.UNDISPATCHED) {
            queue.events.collect {
                received.add(it)
                delivered.complete(Unit)
            }
        }
        try {
            withTimeout(5_000L) { entered.await() }
            assertTrue(received.isEmpty())
            first.cancelAndJoin()
            withTimeout(5_000L) { delivered.await() }
            assertEquals(1, received.size)
        } finally {
            first.cancelAndJoin()
            second.cancelAndJoin()
        }
    }
}