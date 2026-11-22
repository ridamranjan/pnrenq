
package com.first.pnrenq.domain.operation

import org.junit.Assert.*
import org.junit.Test

class PnrOperationCoordinatorTest {

    @Test
    fun firstOperationAcquiresLock() {
        val coordinator = PnrOperationCoordinator()

        assertTrue(coordinator.tryAcquire("1234567890"))
        assertTrue(coordinator.isActive("1234567890"))
    }

    @Test
    fun duplicateOperationIsBlocked() {
        val coordinator = PnrOperationCoordinator()

        assertTrue(coordinator.tryAcquire("1234567890"))
        assertFalse(coordinator.tryAcquire("1234567890"))
    }

    @Test
    fun differentPnrsCanOperateIndependently() {
        val coordinator = PnrOperationCoordinator()

        assertTrue(coordinator.tryAcquire("1234567890"))
        assertTrue(coordinator.tryAcquire("9876543210"))
    }

    @Test
    fun releasingLockAllowsAnotherOperation() {
        val coordinator = PnrOperationCoordinator()

        assertTrue(coordinator.tryAcquire("1234567890"))

        coordinator.release("1234567890")

        assertFalse(coordinator.isActive("1234567890"))
        assertTrue(coordinator.tryAcquire("1234567890"))
    }

    @Test
    fun releasingUnknownPnrDoesNotAffectOtherLocks() {
        val coordinator = PnrOperationCoordinator()

        coordinator.tryAcquire("1234567890")
        coordinator.release("9876543210")

        assertTrue(coordinator.isActive("1234567890"))
    }
}
