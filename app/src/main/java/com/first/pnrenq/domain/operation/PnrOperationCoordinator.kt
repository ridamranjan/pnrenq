
package com.first.pnrenq.domain.operation

class PnrOperationCoordinator {

    private val activePnrs = mutableSetOf<String>()

    @Synchronized
    fun tryAcquire(pnr: String): Boolean {
        if (pnr in activePnrs) return false
        activePnrs.add(pnr)
        return true
    }

    @Synchronized
    fun release(pnr: String) {
        activePnrs.remove(pnr)
    }

    @Synchronized
    fun isActive(pnr: String): Boolean =
        pnr in activePnrs
}
