package com.first.pnrenq.domain.operation

import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PnrOperationCoordinatorConcurrencyTest {

    @Test
    fun simultaneousAttemptsForSamePnrHaveExactlyOneWinner() {
        val coordinator = PnrOperationCoordinator()
        val pnr = "1234567890"
        val results = acquireTogether(coordinator, List(8) { pnr })

        assertEquals(1, results.count { it })
        assertTrue(coordinator.isActive(pnr))
        coordinator.release(pnr)
        assertFalse(coordinator.isActive(pnr))
        assertTrue(coordinator.tryAcquire(pnr))
        coordinator.release(pnr)
    }

    @Test
    fun simultaneousAttemptsForDifferentPnrsCanAllSucceed() {
        val coordinator = PnrOperationCoordinator()
        val pnrs = (1..8).map { it.toString().padStart(10, '0') }
        val results = acquireTogether(coordinator, pnrs)

        assertTrue(results.all { it })
        assertTrue(pnrs.all { coordinator.isActive(it) })
        pnrs.forEach { coordinator.release(it) }
        assertTrue(pnrs.none { coordinator.isActive(it) })
    }

    private fun acquireTogether(
        coordinator: PnrOperationCoordinator,
        pnrs: List<String>
    ): List<Boolean> {
        val executor = Executors.newFixedThreadPool(pnrs.size)
        val ready = CountDownLatch(pnrs.size)
        val start = CountDownLatch(1)
        try {
            val futures = pnrs.map { pnr ->
                executor.submit<Boolean> {
                    ready.countDown()
                    check(start.await(5, TimeUnit.SECONDS))
                    coordinator.tryAcquire(pnr)
                }
            }
            assertTrue(ready.await(5, TimeUnit.SECONDS))
            start.countDown()
            // Retain acquired locks until all contenders have completed.
            return futures.map { it.get(5, TimeUnit.SECONDS) }
        } finally {
            start.countDown()
            executor.shutdownNow()
        }
    }
}