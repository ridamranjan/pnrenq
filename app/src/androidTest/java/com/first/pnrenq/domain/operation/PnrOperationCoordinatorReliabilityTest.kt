
package com.first.pnrenq.domain.operation

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PnrOperationCoordinatorReliabilityTest {

    @Test
    fun samePnrCannotBeAcquiredTwice() {
        val coordinator = PnrOperationCoordinator()
        val pnr = "1234567890"

        assertTrue(coordinator.tryAcquire(pnr))
        assertFalse(coordinator.tryAcquire(pnr))

        coordinator.release(pnr)
        assertTrue(coordinator.tryAcquire(pnr))
    }

    @Test
    fun differentPnrsCanOperateIndependently() {
        val coordinator = PnrOperationCoordinator()

        assertTrue(coordinator.tryAcquire("1234567890"))
        assertTrue(coordinator.tryAcquire("9876543210"))
    }

    @Test
    fun releasingOnePnrDoesNotReleaseAnother() {
        val coordinator = PnrOperationCoordinator()

        coordinator.tryAcquire("1234567890")
        coordinator.tryAcquire("9876543210")

        coordinator.release("1234567890")

        assertFalse(coordinator.isActive("1234567890"))
        assertTrue(coordinator.isActive("9876543210"))
    }

    @Test
    fun repeatedReleaseDoesNotBreakCoordinator() {
        val coordinator = PnrOperationCoordinator()
        val pnr = "1234567890"

        coordinator.release(pnr)
        coordinator.release(pnr)

        assertTrue(coordinator.tryAcquire(pnr))
        coordinator.release(pnr)
        assertFalse(coordinator.isActive(pnr))
    }
}
